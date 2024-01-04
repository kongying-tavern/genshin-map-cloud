package site.yuanshen.genshin.core.service;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.IdUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestMapping;
import site.yuanshen.common.core.utils.BeanUtils;
import site.yuanshen.data.entity.MarkerLinkage;
import site.yuanshen.data.helper.marker.linkage.MarkerLinkageDataHelper;
import site.yuanshen.data.vo.MarkerLinkageSearchVo;
import site.yuanshen.data.vo.MarkerLinkageVo;
import site.yuanshen.data.vo.adapter.marker.linkage.graph.GraphVo;
import site.yuanshen.genshin.core.dao.MarkerLinkageDao;

import java.awt.geom.Point2D;
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
public class MarkerLinkageService {

    private final MarkerLinkageDao markerLinkageDao;
    private final MarkerLinkageHelperService markerLinkageHelperService;

    @Cacheable(value = "listMarkerLinkage")
    public Map<String, List<MarkerLinkageVo>> listMarkerLinkage(MarkerLinkageSearchVo markerLinkageSearchVo) {
        List<String> groupIds = markerLinkageSearchVo.getGroupIds();
        if(CollUtil.isEmpty(groupIds)) {
            return new HashMap<>();
        }

        // 获取关联列表
        final List<MarkerLinkageVo> linkageList = markerLinkageHelperService.getLinkageList(groupIds);
        // 关联路线点位数据
        final List<Long> pathMarkerIds = MarkerLinkageDataHelper.getPathMarkerIdsFromList(linkageList);
        final Map<Long, Point2D.Double> pathMarkerCoords = markerLinkageHelperService.getPathCoords(pathMarkerIds);
        MarkerLinkageDataHelper.patchPathMarkerCoordsInList(linkageList, pathMarkerCoords);

        final Map<String, List<MarkerLinkageVo>> linkageMap = linkageList.parallelStream().collect(Collectors.groupingBy(MarkerLinkageVo::getGroupId));
        return linkageMap;
    }

    @Cacheable(value = "graphMarkerLinkage")
    public Map<String, GraphVo> graphMarkerLinkage(MarkerLinkageSearchVo markerLinkageSearchVo) {
        List<String> groupIds = markerLinkageSearchVo.getGroupIds();
        if(CollUtil.isEmpty(groupIds)) {
            return new HashMap<>();
        }

        // 获取关联绘图数据
        final Map<String, GraphVo> linkageGraph = markerLinkageHelperService.getLinkageGraph(groupIds);
        // 关联路线点位数据
        final List<Long> pathMarkerIds = MarkerLinkageDataHelper.getPathMarkerIdsFromGraph(linkageGraph);
        final Map<Long, Point2D.Double> pathMarkerCoords = markerLinkageHelperService.getPathCoords(pathMarkerIds);
        MarkerLinkageDataHelper.patchPathMarkerCoordsInGraph(linkageGraph, pathMarkerCoords);

        return linkageGraph;
    }

    @Transactional
    public String linkMarker(List<MarkerLinkageVo> linkageVos) {
        // 校验数据可用性
        if(linkageVos == null) linkageVos = new ArrayList<>();
        linkageVos = linkageVos.parallelStream().filter(Objects::nonNull).collect(Collectors.toList());
        markerLinkageHelperService.checkLinkList(linkageVos);

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

}
