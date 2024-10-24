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
import site.yuanshen.common.core.utils.JsonUtils;
import site.yuanshen.common.core.utils.PgsqlUtils;
import site.yuanshen.common.core.utils.SpringContextUtils;
import site.yuanshen.data.dto.MarkerLinkageDto;
import site.yuanshen.data.entity.MarkerLinkage;
import site.yuanshen.data.helper.marker.linkage.MarkerLinkageDataHelper;
import site.yuanshen.data.mapper.MarkerLinkageMapper;
import site.yuanshen.data.vo.MarkerLinkageVo;
import site.yuanshen.data.vo.adapter.cache.MarkerLinkageCacheKeyConst;
import site.yuanshen.data.vo.adapter.marker.linkage.graph.GraphVo;
import site.yuanshen.genshin.core.dao.MarkerLinkageDao;
import site.yuanshen.genshin.core.service.MarkerLinkageHelperService;
import site.yuanshen.genshin.core.service.mbp.MarkerLinkageMBPService;

import java.awt.geom.Point2D;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
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
        final List<Long> allIdList = markerLinkageList
                .stream()
                .map(MarkerLinkage::getId)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        final List<Long> deleteIdList = markerLinkageList
                .stream()
                .filter(v -> v.getId() != null && (v.getDelFlag() == null || Objects.equals(v.getDelFlag(), true)))
                .map(MarkerLinkage::getId)
                .collect(Collectors.toList());
        final List<MarkerLinkage> saveList = markerLinkageList
                .stream()
                .filter(v -> v.getDelFlag() != null && Objects.equals(v.getDelFlag(), false))
                .collect(Collectors.toList());
        final List<MarkerLinkage> insertList = saveList
                .stream()
                .filter(v -> v.getId() == null)
                .collect(Collectors.toList());
        final List<MarkerLinkage> updateList = saveList
                .stream()
                .filter(v -> v.getId() != null)
                .collect(Collectors.toList());

        if(CollUtil.isNotEmpty(allIdList)) {
            markerLinkageMapper.undeleteByIds(PgsqlUtils.unnestLongStr(allIdList));
        }

        boolean processSuccess = true;
        if(CollUtil.isNotEmpty(insertList)) {
            processSuccess = markerLinkageMBPService.saveBatch(insertList, 100);
        }
        if(CollUtil.isNotEmpty(updateList)) {
            processSuccess = processSuccess && markerLinkageMBPService.updateBatchById(updateList, 100);
        }
        if(CollUtil.isNotEmpty(deleteIdList)) {
            processSuccess = processSuccess && markerLinkageMapper.deleteByIds(PgsqlUtils.unnestLongStr(deleteIdList)) >= 0;
        }
        return processSuccess;
    }

    /**
     * 所有的点位关联元数据
     */
    @Override
    @Cacheable(value = "getAllMarkerLinkage")
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
    @Cacheable(value = "listAllMarkerLinkage")
    public Map<String, List<MarkerLinkageVo>> listAllMarkerLinkage() {
        // Allow cache with `FastClassBySpringCGLIB`
        MarkerLinkageDao markerLinkageDao = (MarkerLinkageDao) SpringContextUtils.getBean("markerLinkageDaoImpl");

        // 获取关联列表数据
        final List<MarkerLinkageVo> linkageList = markerLinkageDao.getAllMarkerLinkage();
        final Map<String, List<MarkerLinkageVo>> linkageMap = linkageList.parallelStream().collect(Collectors.groupingBy(MarkerLinkageVo::getGroupId));
        return linkageMap;
    }

    /**
     * 所有的点位关联有向图
     */
    @Override
    @Cacheable(value = "graphAllMarkerLinkage")
    public Map<String, GraphVo> graphAllMarkerLinkage() {
        // Allow cache with `FastClassBySpringCGLIB`
        MarkerLinkageDao markerLinkageDao = (MarkerLinkageDao) SpringContextUtils.getBean("markerLinkageDaoImpl");

        // 获取关联绘图数据
        final List<MarkerLinkageVo> linkageList = markerLinkageDao.getAllMarkerLinkage();
        final Map<String, GraphVo> linkageGraph = MarkerLinkageDataHelper.buildLinkageGraph(linkageList);
        return linkageGraph;
    }

    /**
     * 所有的点位关联列表的压缩
     */
    @Override
    @Cacheable(value = MarkerLinkageCacheKeyConst.MARKER_LINKAGE_LIST_BIN, cacheManager = "neverRefreshCacheManager")
    public byte[] listAllMarkerLinkageBinary() {
        throw new GenshinApiException("缓存未创建");
    }

    /**
     * 刷新点位关联列表压缩缓存并返回压缩文档
     */
    @Override
    @CachePut(value = MarkerLinkageCacheKeyConst.MARKER_LINKAGE_LIST_BIN, cacheManager = "neverRefreshCacheManager")
    public byte[] refreshAllMarkerLinkageListBinary() {
        try {
            final Map<String, List<MarkerLinkageVo>> linkageList = listAllMarkerLinkage();
            final byte[] result = JSON.toJSONString(linkageList, JsonUtils.defaultWriteFeatures).getBytes(StandardCharsets.UTF_8);
            return CompressUtils.compress(result);
        } catch (Exception e) {
            throw new GenshinApiException("创建压缩失败", e);
        }
    }

    /**
     * 所有的点位关联有向图的压缩
     */
    @Override
    @Cacheable(value = MarkerLinkageCacheKeyConst.MARKER_LINKAGE_GRAPH_BIN, cacheManager = "neverRefreshCacheManager")
    public byte[] graphAllMarkerLinkageBinary() {
        throw new GenshinApiException("缓存未创建");
    }

    /**
     * 刷新点位关联有向图压缩缓存并返回压缩文档
     */
    @Override
    @CachePut(value = MarkerLinkageCacheKeyConst.MARKER_LINKAGE_GRAPH_BIN, cacheManager = "neverRefreshCacheManager")
    public byte[] refreshAllMarkerLinkageGraphBinary() {
        try {
            final Map<String, GraphVo> linkageGraph = graphAllMarkerLinkage();
            final byte[] result = JSON.toJSONString(linkageGraph, JsonUtils.defaultWriteFeatures).getBytes(StandardCharsets.UTF_8);
            return CompressUtils.compress(result);
        } catch (Exception e) {
            throw new GenshinApiException("创建压缩失败", e);
        }
    }
}
