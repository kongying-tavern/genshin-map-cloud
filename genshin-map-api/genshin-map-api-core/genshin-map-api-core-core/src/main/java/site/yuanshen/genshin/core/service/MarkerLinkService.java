package site.yuanshen.genshin.core.service;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.*;
import cn.hutool.crypto.SecureUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestMapping;
import site.yuanshen.common.core.exception.GenshinApiException;
import site.yuanshen.common.core.utils.BeanUtils;
import site.yuanshen.data.entity.MarkerLinkage;
import site.yuanshen.data.vo.MarkerLinkageSearchVo;
import site.yuanshen.data.vo.MarkerLinkageVo;
import site.yuanshen.genshin.core.dao.MarkerLinkageDao;
import site.yuanshen.genshin.core.service.mbp.MarkerLinkageMBPService;

import java.nio.ByteBuffer;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 点位关联服务接口实现
 *
 * @author Alex Fang
 */
@Service
@RequestMapping
@RequiredArgsConstructor
public class MarkerLinkService {

    private final MarkerLinkageDao markerLinkageDao;
    private final MarkerLinkageMBPService markerLinkageMBPService;

    @Cacheable(value = "listMarkerLinkage")
    public Map<String, List<MarkerLinkageVo>> listLinkage(MarkerLinkageSearchVo markerLinkageSearchVo) {
        List<String> groupIds = markerLinkageSearchVo.getGroupIds();
        if(CollUtil.isEmpty(groupIds)) {
            return new HashMap<>();
        }

        List<MarkerLinkageVo> linkageList = markerLinkageMBPService.list(Wrappers.<MarkerLinkage>lambdaQuery().in(MarkerLinkage::getGroupId, groupIds)).stream()
            .map(markerLinkage -> BeanUtils.copy(markerLinkage, MarkerLinkageVo.class)).collect(Collectors.toList());
        reverseLinkageIds(linkageList);
        Map<String, List<MarkerLinkageVo>> linkageMap = linkageList.parallelStream().collect(Collectors.groupingBy(MarkerLinkageVo::getGroupId));

        return linkageMap;
    }

    @Transactional
    public String linkMarker(List<MarkerLinkageVo> linkageVos) {
        // 校验数据可用性
        if(linkageVos == null) linkageVos = new ArrayList<>();
        linkageVos = linkageVos.parallelStream().filter(Objects::nonNull).collect(Collectors.toList());
        checkLinkList(linkageVos);

        final String groupId = IdUtil.fastSimpleUUID();

        // 获取现有的列表
        final List<Long> idList = this.getLinkIdList(linkageVos);
        final List<MarkerLinkage> linkageExistsList = markerLinkageDao.getRelatedLinkageList(idList, true)
            .parallelStream()
            .map(v -> BeanUtils.copy(v, MarkerLinkage.class))
            .collect(Collectors.toList());

        // 生成更新数据的数据列表
        Map<String, MarkerLinkage> linkageMap = this.getLinkSearchMap(linkageExistsList);
        linkageMap = this.patchLinkSearchMap(linkageMap, linkageVos, groupId);
        List<MarkerLinkage> linkageList = new ArrayList<>(linkageMap.values());

        // 更新数据
        boolean linkSuccess = markerLinkageDao.saveOrUpdateBatch(linkageList);
        return linkSuccess ? groupId : "";
    }

    private void reverseLinkageIds(List<MarkerLinkageVo> linkageVos) {
        for(MarkerLinkageVo linkageVo : linkageVos) {
            final Long fromId = ObjectUtil.defaultIfNull(linkageVo.getFromId(), 0L);
            final Long toId = ObjectUtil.defaultIfNull(linkageVo.getToId(), 0L);
            final Boolean linkReverse = ObjectUtil.defaultIfNull(linkageVo.getLinkReverse(), false);

            if(linkReverse) {
                linkageVo.setFromId(toId);
                linkageVo.setToId(fromId);
                linkageVo.setLinkReverse(false);
            }
        }
    }

    private void checkLinkList(List<MarkerLinkageVo> linkageVos) {
        for(MarkerLinkageVo linkageVo : linkageVos) {
            final Long fromId = linkageVo.getFromId();
            final Long toId = linkageVo.getToId();
            if(fromId == null || fromId.compareTo(0L) <= 0 || toId == null || toId.compareTo(0L) <= 0) {
                throw new GenshinApiException("无效的关联节点ID");
            } else if(fromId.compareTo(toId) == 0) {
                throw new GenshinApiException("不能将点位关联到自身");
            }
        }
        if(CollUtil.isEmpty(linkageVos)) {
            throw new GenshinApiException("关联数据不可为空");
        }
    }

    private List<Long> getLinkIdList(List<MarkerLinkageVo> linkageVos) {
        // 生成用到的 ID
        final Set<Long> idSet = new HashSet<>();
        for(MarkerLinkageVo linkage : linkageVos) {
            idSet.add(linkage.getFromId());
            idSet.add(linkage.getToId());
        }
        return idSet.parallelStream().filter(Objects::nonNull).collect(Collectors.toList());
    }

    private Map<String, MarkerLinkage> getLinkSearchMap(List<MarkerLinkage> linkageVos) {
        final Map<String, MarkerLinkage> searchMap = new HashMap<>();
        for(MarkerLinkage linkageEntity : linkageVos) {
            final String idHash = getIdHash(Arrays.asList(linkageEntity.getFromId(), linkageEntity.getToId()));
            searchMap.put(idHash, linkageEntity);
        }
        return searchMap;
    }

    private Map<String, MarkerLinkage> patchLinkSearchMap(Map<String, MarkerLinkage> linkageMap, List<MarkerLinkageVo> linkageVos, String groupId) {
        // 先设置所有的关联为删除，后续对新增关联开启，以便复用现有关联
        linkageMap.replaceAll((hash, v) -> {
            v.setDelFlag(true);
            return v;
        });

        for(MarkerLinkageVo linkageVo : linkageVos) {
            Long fromId = linkageVo.getFromId();
            Long toId = linkageVo.getToId();
            if(fromId == null || toId == null) {
                continue;
            }
            boolean dirReverse = false;
            if(fromId.compareTo(toId) > 0) {
                fromId = fromId ^ toId;
                toId = fromId ^ toId;
                fromId = fromId ^ toId;
                dirReverse = true;
            }
            final String idHash = this.getIdHash(Arrays.asList(linkageVo.getFromId(), linkageVo.getToId()));
            final MarkerLinkage linkageItem = linkageMap.getOrDefault(idHash, new MarkerLinkage());
            linkageItem.setGroupId(groupId);
            linkageItem.setFromId(fromId);
            linkageItem.setToId(toId);
            linkageItem.setLinkAction(StrUtil.blankToDefault(linkageVo.getLinkAction(), ""));
            linkageItem.setLinkReverse(dirReverse);
            linkageItem.setPath(CollUtil.defaultIfEmpty(linkageVo.getPath(), List.of()));
            linkageItem.setDelFlag(false);

            linkageMap.put(idHash, linkageItem);
        }

        return linkageMap;
    }

    private String getIdHash(Collection<Long> idList) {
        idList = idList.stream().map(id -> ObjectUtil.defaultIfNull(id, 0L)).sorted().collect(Collectors.toList());
        ByteBuffer buffer = ByteBuffer.allocate(Long.SIZE / Byte.SIZE * idList.size());
        idList.forEach(buffer::putLong);
        final String idHash = SecureUtil.md5(buffer.toString());
        return idHash;
    }
}
