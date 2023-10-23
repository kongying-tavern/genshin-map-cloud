package site.yuanshen.genshin.core.dao.impl;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import site.yuanshen.common.core.utils.PgsqlUtils;
import site.yuanshen.data.entity.MarkerLinkage;
import site.yuanshen.data.mapper.MarkerLinkageMapper;
import site.yuanshen.genshin.core.dao.MarkerLinkageDao;
import site.yuanshen.genshin.core.service.mbp.MarkerLinkageMBPService;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 点位关联的数据查询层实现
 *
 * @author Alex Fang
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MarkerLinkageDaoImpl implements MarkerLinkageDao {

    private final MarkerLinkageMapper markerLinkageMapper;
    private final MarkerLinkageMBPService markerLinkageMBPService;

    @Override
    public List<MarkerLinkage> getRelatedLinkageList(List<Long> idList, boolean includeDeleted) {
        if(CollUtil.isEmpty(idList)) {
            return new ArrayList<>();
        }

        // 根据 ID 获取列表
        final List<MarkerLinkage> listWithIds = markerLinkageMapper.selectWithLargeMarkerIdIn(
            PgsqlUtils.unnestLongStr(idList),
            Wrappers.<MarkerLinkage>lambdaQuery().eq(!includeDeleted, MarkerLinkage::getDelFlag, false)
        );

        // 根据 组ID 获取列表
        final List<String> groupIdList = listWithIds.parallelStream().map(MarkerLinkage::getGroupId).distinct().collect(Collectors.toList());
        List<MarkerLinkage> listWithGroupIds = new ArrayList<>();
        if(CollUtil.isNotEmpty(groupIdList)) {
            listWithGroupIds = markerLinkageMapper.selectWithLargeCustomIn(
                "group_id",
                "varchar",
                PgsqlUtils.unnestStringStr(groupIdList),
                Wrappers.<MarkerLinkage>lambdaQuery().eq(!includeDeleted, MarkerLinkage::getDelFlag, false)
            );
        }

        // 合并数据
        List<MarkerLinkage> list = new ArrayList<>(Stream.of(listWithIds, listWithGroupIds)
                .flatMap(List::stream)
                .collect(Collectors.toMap(
                        MarkerLinkage::getId,
                        v -> v,
                        (o, n) -> n
                ))
                .values());

        return list;
    }

    @Override
    public boolean removeRelatedLinkageList(List<Long> idList, boolean includeDeleted) {
        List<MarkerLinkage> linkageList = this.getRelatedLinkageList(idList, includeDeleted);
        if(CollUtil.isEmpty(linkageList)) {
            return true;
        }
        int deletedCount = markerLinkageMapper.deleteBatchIds(linkageList.stream().map(MarkerLinkage::getId).collect(Collectors.toList()));
        return deletedCount >= 0;
    }

    @Override
    public boolean saveOrUpdateBatch(List<MarkerLinkage> markerLinkageList) {
        final List<Long> deleteIdList = markerLinkageList.parallelStream().filter(v -> v.getId() != null && v.getDelFlag() == null || v.getDelFlag().equals(true)).map(MarkerLinkage::getId).collect(Collectors.toList());
        final List<MarkerLinkage> updateList = markerLinkageList.parallelStream().filter(v -> v.getDelFlag() != null && v.getDelFlag().equals(false)).collect(Collectors.toList());

        boolean updateSuccess = CollUtil.isEmpty(markerLinkageList) || markerLinkageMBPService.saveOrUpdateBatch(updateList);
        int deleteSuccess = CollUtil.isEmpty(deleteIdList) ? 1 : markerLinkageMapper.deleteBatchIds(deleteIdList);
        return updateSuccess && deleteSuccess >= 0;
    }
}
