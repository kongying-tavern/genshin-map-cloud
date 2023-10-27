package site.yuanshen.genshin.core.service;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestMapping;
import site.yuanshen.common.core.exception.GenshinApiException;
import site.yuanshen.common.core.utils.BeanUtils;
import site.yuanshen.data.entity.MarkerLinkage;
import site.yuanshen.data.helper.MarkerLinkageDataHelper;
import site.yuanshen.data.vo.MarkerLinkageSearchVo;
import site.yuanshen.data.vo.MarkerLinkageVo;
import site.yuanshen.genshin.core.dao.MarkerLinkageDao;
import site.yuanshen.genshin.core.service.mbp.MarkerLinkageMBPService;

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
        MarkerLinkageDataHelper.reverseLinkageIds(linkageList);
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
        final List<Long> idList = MarkerLinkageDataHelper.getLinkIdList(linkageVos);
        final List<MarkerLinkage> linkageExistsList = markerLinkageDao.getRelatedLinkageList(idList, true)
            .parallelStream()
            .map(v -> BeanUtils.copy(v, MarkerLinkage.class))
            .collect(Collectors.toList());

        // 生成更新数据的数据列表
        Map<String, MarkerLinkage> linkageMap = MarkerLinkageDataHelper.getLinkSearchMap(linkageExistsList);
        linkageMap = MarkerLinkageDataHelper.patchLinkSearchMap(linkageMap, linkageVos, groupId);
        List<MarkerLinkage> linkageList = new ArrayList<>(linkageMap.values());

        // 更新数据
        boolean linkSuccess = markerLinkageDao.saveOrUpdateBatch(linkageList);
        return linkSuccess ? groupId : "";
    }

    // -------------------------------------------
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
}
