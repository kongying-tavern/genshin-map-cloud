package site.yuanshen.genshin.core.dao.impl;

import cn.hutool.core.collection.CollUtil;
import com.alibaba.fastjson2.JSON;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import site.yuanshen.common.core.exception.GenshinApiException;
import site.yuanshen.common.core.utils.CompressUtils;
import site.yuanshen.common.core.utils.PgsqlUtils;
import site.yuanshen.common.core.utils.SpringContextUtils;
import site.yuanshen.data.dto.MarkerLinkageDto;
import site.yuanshen.data.entity.MarkerLinkage;
import site.yuanshen.data.helper.MarkerLinkageDataHelper;
import site.yuanshen.data.mapper.MarkerLinkageMapper;
import site.yuanshen.data.vo.MarkerLinkageVo;
import site.yuanshen.data.vo.adapter.marker.linkage.graph.GraphVo;
import site.yuanshen.genshin.core.dao.MarkerLinkageDao;
import site.yuanshen.genshin.core.service.MarkerLinkageHelperService;
import site.yuanshen.genshin.core.service.mbp.MarkerLinkageMBPService;

import java.awt.geom.Point2D;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
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
    private final MarkerLinkageHelperService markerLinkageHelperService;

    /**
     * 获取相关的点位关联列表
     */
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

    /**
     * 删除相关的点位关联数据
     */
    @Override
    public boolean removeRelatedLinkageList(List<Long> idList, boolean includeDeleted) {
        List<MarkerLinkage> linkageList = this.getRelatedLinkageList(idList, includeDeleted);
        if(CollUtil.isEmpty(linkageList)) {
            return true;
        }
        int deletedCount = markerLinkageMapper.deleteBatchIds(linkageList.stream().map(MarkerLinkage::getId).collect(Collectors.toList()));
        return deletedCount >= 0;
    }

    /**
     * 保存点位关联
     */
    @Override
    public boolean saveOrUpdateBatch(List<MarkerLinkage> markerLinkageList) {
        final List<Long> deleteIdList = markerLinkageList.parallelStream().filter(v -> v.getId() != null && v.getDelFlag() == null || v.getDelFlag().equals(true)).map(MarkerLinkage::getId).collect(Collectors.toList());
        final List<MarkerLinkage> updateList = markerLinkageList.parallelStream().filter(v -> v.getDelFlag() != null && v.getDelFlag().equals(false)).collect(Collectors.toList());

        boolean updateSuccess = CollUtil.isEmpty(markerLinkageList) || markerLinkageMBPService.saveOrUpdateBatch(updateList);
        int deleteSuccess = CollUtil.isEmpty(deleteIdList) ? 1 : markerLinkageMapper.deleteBatchIds(deleteIdList);
        return updateSuccess && deleteSuccess >= 0;
    }

    /**
     * 所有的点位关联元数据
     */
    @Override
    @Cacheable("getAllMarkerLinkage")
    public List<MarkerLinkageVo> getAllMarkerLinkage() {
        final List<MarkerLinkage> linkageList = markerLinkageMBPService.list();
        if(CollUtil.isEmpty(linkageList)) {
            return new ArrayList<>();
        }

        // 获取列表
        final List<MarkerLinkageVo> linkageVoList = linkageList.parallelStream()
                .map(MarkerLinkageDto::new)
                .map(MarkerLinkageDto::getVo)
                .collect(Collectors.toList());
        MarkerLinkageDataHelper.reverseLinkageIds(linkageVoList);

        // 关联路线点位数据
        final List<Long> pathMarkerIds = MarkerLinkageDataHelper.getPathMarkerIdsFromList(linkageVoList);
        final Map<Long, Point2D.Double> pathMarkerCoords = markerLinkageHelperService.getPathCoords(pathMarkerIds);
        MarkerLinkageDataHelper.patchPathMarkerCoordsInList(linkageVoList, pathMarkerCoords);

        return linkageVoList;
    }

    /**
     * 所有的点位关联列表
     */
    @Override
    public Map<String, List<MarkerLinkageVo>> listAllMarkerLinkage() {
        return null;
    }

    /**
     * 所有的点位关联有向图
     */
    @Override
    @Cacheable("graphAllMarkerLinkage")
    public Map<String, GraphVo> graphAllMarkerLinkage() {
        // Allow cache with `FastClassBySpringCGLIB`
        MarkerLinkageDao markerLinkageDao = (MarkerLinkageDao) SpringContextUtils.getBean("markerLinkageDaoImpl");

        // 获取关联绘图数据
        final List<MarkerLinkageVo> linkageList = markerLinkageDao.getAllMarkerLinkage();
        final Map<String, GraphVo> linkageGraph = MarkerLinkageDataHelper.buildLinkageGraph(linkageList);
        return linkageGraph;
    }

    /**
     * 所有的点位关联列表的Bz2压缩
     */
    @Override
    @Cacheable(value = "listAllMarkerLinkageBz2", cacheManager = "neverRefreshCacheManager")
    public byte[] listAllMarkerLinkageBz2() {
        throw new GenshinApiException("缓存未创建");
    }

    /**
     * 刷新点位关联列表压缩缓存并返回压缩文档
     */
    @Override
    @CachePut(value = "listAllMarkerLinkageBz2", cacheManager = "neverRefreshCacheManager")
    public byte[] refreshAllMarkerLinkageListBz2() {
        try {
            final List<MarkerLinkageVo> linkageList = getAllMarkerLinkage();
            final byte[] result = JSON.toJSONString(linkageList).getBytes(StandardCharsets.UTF_8);
            return CompressUtils.compress(result);
        } catch (Exception e) {
            throw new GenshinApiException("创建压缩失败", e);
        }
    }

    /**
     * 所有的点位关联有向图的Bz2压缩
     */
    @Override
    @Cacheable(value = "graphAllMarkerLinkageBz2", cacheManager = "neverRefreshCacheManager")
    public byte[] graphAllMarkerLinkageBz2() {
        throw new GenshinApiException("缓存未创建");
    }

    /**
     * 刷新点位关联有向图压缩缓存并返回压缩文档
     */
    @Override
    @CachePut(value = "graphAllMarkerLinkageBz2", cacheManager = "neverRefreshCacheManager")
    public byte[] refreshAllMarkerLinkageGraphBz2() {
        try {
            final Map<String, GraphVo> linkageGraph = graphAllMarkerLinkage();
            final byte[] result = JSON.toJSONString(linkageGraph).getBytes(StandardCharsets.UTF_8);
            return CompressUtils.compress(result);
        } catch (Exception e) {
            throw new GenshinApiException("创建压缩失败", e);
        }
    }
}
