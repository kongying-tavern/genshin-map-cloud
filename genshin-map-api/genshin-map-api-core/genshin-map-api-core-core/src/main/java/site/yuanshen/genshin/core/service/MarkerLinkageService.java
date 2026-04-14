package site.yuanshen.genshin.core.service;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.BooleanUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestMapping;
import site.yuanshen.common.core.utils.BeanUtils;
import site.yuanshen.common.core.utils.PgsqlUtils;
import site.yuanshen.data.dto.MarkerLinkageDto;
import site.yuanshen.data.entity.MarkerLinkage;
import site.yuanshen.data.helper.marker.linkage.MarkerLinkageDataHelper;
import site.yuanshen.data.mapper.MarkerLinkageMapper;
import site.yuanshen.data.vo.MarkerLinkageSearchVo;
import site.yuanshen.data.vo.MarkerLinkageVo;
import site.yuanshen.data.vo.adapter.marker.linkage.LinkChangeVo;
import site.yuanshen.data.vo.adapter.marker.linkage.LinkDeleteQueryVo;
import site.yuanshen.data.vo.adapter.marker.linkage.LinkDeleteVo;
import site.yuanshen.data.vo.adapter.marker.linkage.graph.GraphVo;
import site.yuanshen.genshin.core.dao.MarkerLinkageDao;
import site.yuanshen.genshin.core.service.mbp.MarkerMBPService;

import java.awt.geom.Point2D;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 点位关联服务接口实现
 *
 * @author Alex Fang
 */
@Service
@RequestMapping
@RequiredArgsConstructor
public class MarkerLinkageService {
    private final MarkerLinkageMapper markerLinkageMapper;
    private final MarkerMBPService markerMBPService;
    private final MarkerLinkageDao markerLinkageDao;
    private final MarkerLinkageHelperService markerLinkageHelperService;

    @Cacheable(value = "listMarkerLinkage")
    public Map<String, List<MarkerLinkageVo>> listMarkerLinkage(MarkerLinkageSearchVo markerLinkageSearchVo) {
        final Boolean isTraverse = BooleanUtil.isTrue(markerLinkageSearchVo.getIsTraverse());
        final List<String> groupIds = markerLinkageSearchVo.getGroupIds();

        // 获取关联列表
        final List<MarkerLinkageVo> linkageList = markerLinkageHelperService.getLinkageList(isTraverse, groupIds);
        // 关联路线点位数据
        final List<Long> pathMarkerIds = MarkerLinkageDataHelper.getPathMarkerIdsFromList(linkageList);
        final Map<Long, Point2D.Double> pathMarkerCoords = markerLinkageHelperService.getPathCoords(pathMarkerIds);
        MarkerLinkageDataHelper.patchPathMarkerCoordsInList(linkageList, pathMarkerCoords);

        final Map<String, List<MarkerLinkageVo>> linkageMap = linkageList.parallelStream().collect(Collectors.groupingBy(MarkerLinkageVo::getGroupId));
        return linkageMap;
    }

    @Cacheable(value = "graphMarkerLinkage")
    public Map<String, GraphVo> graphMarkerLinkage(MarkerLinkageSearchVo markerLinkageSearchVo) {
        final Boolean isTraverse = BooleanUtil.isTrue(markerLinkageSearchVo.getIsTraverse());
        final List<String> groupIds = markerLinkageSearchVo.getGroupIds();

        // 获取关联绘图数据
        final Map<String, GraphVo> linkageGraph = markerLinkageHelperService.getLinkageGraph(isTraverse, groupIds);
        // 关联路线点位数据
        final List<Long> pathMarkerIds = MarkerLinkageDataHelper.getPathMarkerIdsFromGraph(linkageGraph);
        final Map<Long, Point2D.Double> pathMarkerCoords = markerLinkageHelperService.getPathCoords(pathMarkerIds);
        MarkerLinkageDataHelper.patchPathMarkerCoordsInGraph(linkageGraph, pathMarkerCoords);

        return linkageGraph;
    }

    @Transactional
    public String linkMarker(List<MarkerLinkageVo> linkageVos, LinkChangeVo changeVo) {
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
        final Set<String> linkExGroupIds = linkageExistsList
                .parallelStream()
                .map(MarkerLinkage::getGroupId)
                .filter(StrUtil::isNotBlank)
                .collect(Collectors.toSet());
        final List<Long> linkExMarkerIds = MarkerLinkageDataHelper.getLinkIdList(
                linkageExistsList
                        .parallelStream()
                        .map(MarkerLinkageDto::new)
                        .map(MarkerLinkageDto::getVo)
                        .collect(Collectors.toList())
        );

        // 生成更新数据的数据列表
        Map<String, MarkerLinkage> linkageMap = MarkerLinkageDataHelper.getLinkSearchMap(linkageExistsList);
        linkageMap = MarkerLinkageDataHelper.patchLinkSearchMap(linkageMap, linkageVos, groupId);
        List<MarkerLinkage> linkageList = new ArrayList<>(linkageMap.values());

        // 更新数据
        boolean linkSuccess = markerLinkageDao.saveOrUpdateBatch(linkageList);
        String linkGroupId = linkSuccess ? groupId : "";

        // (*) 将受到影响的数据添加到变更数据中
        // 添加变更前数据
        changeVo.addGroups(linkExGroupIds);
        changeVo.addMarkers(linkExMarkerIds);
        // 添加用户提交的关联数据
        changeVo.addMarkers(idList);
        // 添加新创建的关联组
        if(StrUtil.isNotBlank(linkGroupId)) {
            changeVo.addGroups(Set.of(linkGroupId));
        }

        return linkGroupId;
    }

    @Transactional
    public void deleteMarkerLinkage(LinkDeleteQueryVo linkageDeleteVo, LinkDeleteVo deleteVo) {
        if (linkageDeleteVo == null)
            return;

        List<MarkerLinkage> listWithIds = List.of();
        List<MarkerLinkage> listWithGroupIds = List.of();
        if (!CollUtil.isEmpty(linkageDeleteVo.getIds())) {
            listWithIds = markerLinkageMapper.selectWithLargeCustomIn(
                "id",
                "bigint",
                PgsqlUtils.unnestLongStr(linkageDeleteVo.getIds()),
                Wrappers.<MarkerLinkage>lambdaQuery().eq(MarkerLinkage::getDelFlag, false)
            );
        }
        if (!CollUtil.isEmpty(linkageDeleteVo.getGroupIds())) {
            listWithGroupIds = markerLinkageMapper.selectWithLargeCustomIn(
                "group_id",
                "varchar",
                PgsqlUtils.unnestStringStr(linkageDeleteVo.getGroupIds()),
                Wrappers.<MarkerLinkage>lambdaQuery().eq(MarkerLinkage::getDelFlag, false)
            );
        }

        // 合并数据
        List<MarkerLinkageVo> list = new ArrayList<>(
            Stream.of(listWithIds, listWithGroupIds)
                .flatMap(List::stream)
                .collect(Collectors.toMap(
                        MarkerLinkage::getId,
                        v -> v,
                        (o, n) -> n
                    )
                )
                .values())
            .parallelStream()
            .map(link -> new MarkerLinkageDto(link).getVo())
            .collect(Collectors.toList());

        // 获取数据字段
        List<Long> markerIds = MarkerLinkageDataHelper.getLinkIdList(list);
        List<String> groupIds = list.stream()
            .map(link -> link != null ? link.getGroupId() : null)
            .filter(Objects::nonNull)
            .distinct()
            .collect(Collectors.toList());
        List<Long> idList = list.stream()
            .map(v -> v != null ? v.getId() : null)
            .filter(Objects::nonNull)
            .distinct()
            .collect(Collectors.toList());

        if (!CollUtil.isEmpty(idList)) {
            // 删除数据
            markerLinkageMapper.deleteByIds(PgsqlUtils.unnestLongStr(idList));

            // (*) 将受到影响的数据添加到变更数据中
            deleteVo.addGroups(groupIds);
            deleteVo.addMarkers(markerIds);
        }
    }
}
