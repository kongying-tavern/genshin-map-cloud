package site.yuanshen.data.helper.marker.linkage;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ByteUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.SecureUtil;
import org.apache.logging.log4j.util.TriConsumer;
import site.yuanshen.data.dto.adapter.marker.linkage.graph.*;
import site.yuanshen.data.entity.MarkerLinkage;
import site.yuanshen.data.enums.marker.linkage.IdTypeEnum;
import site.yuanshen.data.enums.marker.linkage.LinkActionEnum;
import site.yuanshen.data.enums.marker.linkage.RelationTypeEnum;
import site.yuanshen.data.vo.MarkerLinkageVo;
import site.yuanshen.data.vo.adapter.marker.linkage.graph.GraphVo;

import java.awt.geom.Point2D;
import java.nio.ByteOrder;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class MarkerLinkageDataHelper {
    //////////////START:通用方法//////////////
    public static void reverseLinkageIds(List<MarkerLinkageVo> linkageVos) {
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

    public static List<Long> getLinkIdList(List<MarkerLinkageVo> linkageVos) {
        // 生成用到的 ID
        final Set<Long> idSet = new HashSet<>();
        for(MarkerLinkageVo linkage : linkageVos) {
            idSet.add(linkage.getFromId());
            idSet.add(linkage.getToId());
        }
        return idSet.parallelStream().filter(Objects::nonNull).collect(Collectors.toList());
    }

    public static String getIdHash(List<Long> idList) {
        idList = idList.stream().map(id -> ObjectUtil.defaultIfNull(id, 0L)).sorted().collect(Collectors.toList());
        int byteSize = Long.BYTES * idList.size();
        byte[] bytes = new byte[byteSize];

        for(int i = 0; i < idList.size(); i++) {
            Long id = idList.get(i);
            byte[] idBytes = ByteUtil.longToBytes(id, ByteOrder.BIG_ENDIAN);
            System.arraycopy(idBytes, 0, bytes, i * Long.BYTES, Long.BYTES);
        }
        final String idHash = SecureUtil.md5(Arrays.toString(bytes));
        return idHash;
    }

    public static AccumulatorKey getAccumulateKey(MarkerLinkageVo linkage) {
        final String groupId = StrUtil.blankToDefault(linkage.getGroupId(), "");
        final String linkAction = StrUtil.blankToDefault(linkage.getLinkAction(), "");
        final LinkActionEnum linkActionEnum = LinkActionEnum.find(linkAction);
        return new AccumulatorKey()
            .withGroupId(groupId)
            .withLinkAction(linkActionEnum);
    }

    public static LinkRefDto getLinkRef(MarkerLinkageVo linkage) {
        final Long linkId = ObjectUtil.defaultIfNull(linkage.getId(), 0L);
        final Long fromId = ObjectUtil.defaultIfNull(linkage.getFromId(), 0L);
        final Long toId = ObjectUtil.defaultIfNull(linkage.getToId(), 0L);

        return new LinkRefDto()
                .withFromId(fromId)
                .withToId(toId)
                .withPathRefId(linkId);
    }

    public static DistributorKey getDistributeKey(AccumulatorKey accumulatorKey, String linkGroupId, Long id) {
        final String groupId = StrUtil.blankToDefault(accumulatorKey.getGroupId(), "");
        final LinkActionEnum linkAction = accumulatorKey.getLinkAction();
        final String linkActionName = linkAction == null ? "" : StrUtil.blankToDefault(linkAction.getValue(), "");
        final Long markerId = ObjectUtil.defaultIfNull(id, 0L);
        return new DistributorKey()
                .withGroupId(groupId)
                .withLinkGroupId(linkGroupId)
                .withLinkAction(linkActionName)
                .withMarkerId(markerId);
    }

    public static RelationDto getRelationGroup(Set<LinkRefDto> linkRefs, LinkActionEnum linkAction) {
        linkRefs = CollUtil.defaultIfEmpty(linkRefs, new HashSet<>());
        final RelationDto relation = new RelationDto();

        switch (linkAction) {
            case TRIGGER:
            case TRIGGER_ALL:
            case TRIGGER_ANY:
                relation.setType(linkAction.getValue());
                for(LinkRefDto linkRef : linkRefs) {
                    relation.addRelation(RelationTypeEnum.TRIGGER, IdTypeEnum.FROM, linkRef);
                    relation.addRelation(RelationTypeEnum.TARGET, IdTypeEnum.TO, linkRef);
                }
                break;
            case RELATED:
            case EQUIVALENT:
                relation.setType(linkAction.getValue());
                for(LinkRefDto linkRef : linkRefs) {
                    relation.addRelation(RelationTypeEnum.GROUP, IdTypeEnum.FROM, linkRef);
                    relation.addRelation(RelationTypeEnum.GROUP, IdTypeEnum.TO, linkRef);
                }
                break;
            default:
        }
        return relation;
    }
    //////////////END:通用方法//////////////

    //////////////START:绘图数据方法//////////////
    public static Map<String, GraphVo> buildLinkageGraph(List<MarkerLinkageVo> linkageVos) {
        ConcurrentHashMap<AccumulatorKey, List<MarkerLinkageVo>> graphSearchMap = getGraphSearchMap(linkageVos);

        // 聚合数据为行为分组
        ConcurrentHashMap<AccumulatorKey, List<AccumulatorCache>> accumulateMap = new ConcurrentHashMap<>();
        accumulateGraphData(accumulateMap, graphSearchMap);

        // 分散分组为与点位关联的数据
        ConcurrentHashMap<DistributorKey, DistributorDto> graphDistributeMap = new ConcurrentHashMap<>();
        distributeGraphData(graphDistributeMap, accumulateMap);

        // 聚合点位关联数据为 API 数据
        Map<String, GraphVo> graphData = restructureGraphData(graphDistributeMap);

        return graphData;
    }

    private static ConcurrentHashMap<AccumulatorKey, List<MarkerLinkageVo>> getGraphSearchMap(List<MarkerLinkageVo> linkageVos) {
        return linkageVos.parallelStream()
            .filter(Objects::nonNull)
            .map(linkage -> {
                final String groupId = StrUtil.blankToDefault(linkage.getGroupId(), "");
                final Long fromId = ObjectUtil.defaultIfNull(linkage.getFromId(), 0L);
                final Long toId = ObjectUtil.defaultIfNull(linkage.getToId(), 0L);
                final LinkActionEnum linkAction = LinkActionEnum.find(linkage.getLinkAction());
                if(StrUtil.isBlank(groupId) || fromId.compareTo(0L) <= 0 || toId.compareTo(0L) <= 0 || linkAction == null) {
                    return null;
                }
                return linkage
                    .withGroupId(groupId)
                    .withFromId(fromId)
                    .withToId(toId);
            })
            .filter(Objects::nonNull)
            .collect(
                Collectors.groupingByConcurrent(MarkerLinkageDataHelper::getAccumulateKey, ConcurrentHashMap::new, Collectors.toList())
            );
    }

    private static void accumulateGraphData(
        Map<AccumulatorKey, List<AccumulatorCache>> accumulateMap,
        ConcurrentHashMap<AccumulatorKey, List<MarkerLinkageVo>> graphSearchMap
    ) {
        graphSearchMap.forEachEntry(2, markerLinkageEntity -> {
            final AccumulatorKey key = markerLinkageEntity.getKey();
            final List<MarkerLinkageVo> valList = markerLinkageEntity.getValue();

            final LinkActionEnum linkAction = key.getLinkAction();
            if(linkAction == null) {
                return;
            }

            final BiConsumer<List<AccumulatorCache>, MarkerLinkageVo> linkAccumulator = linkAction.getAccumulator();
            if(linkAccumulator != null && CollUtil.isNotEmpty(valList)) {
                accumulateMap.putIfAbsent(key, new ArrayList<>());
                List<AccumulatorCache> accumulateList = accumulateMap.get(key);
                valList.parallelStream().forEach(linkage -> linkAccumulator.accept(accumulateList, linkage));
            }
        });
    }

    private static void distributeGraphData(
            Map<DistributorKey, DistributorDto> graphDistributeMap,
            ConcurrentHashMap<AccumulatorKey, List<AccumulatorCache>> accumulateMap
    ) {
        accumulateMap.forEachEntry(2, accEntry -> {
            final AccumulatorKey key = accEntry.getKey();
            final LinkActionEnum linkAction = key.getLinkAction();
            if(linkAction == null) {
                return;
            }

            final TriConsumer<Map<DistributorKey, DistributorDto>, AccumulatorKey, AccumulatorCache> linkDistributor = linkAction.getDistributor();
            if(linkDistributor != null) {
                final List<AccumulatorCache> cacheList = accEntry.getValue();
                cacheList.parallelStream().forEach(cache -> {
                    linkDistributor.accept(graphDistributeMap, key, cache);
                });
            }
        });
    }

    private static Map<String, GraphVo> restructureGraphData(ConcurrentHashMap<DistributorKey, DistributorDto> graphDistributeMap) {
        final ConcurrentHashMap<String, GraphDto> graphMap = new ConcurrentHashMap<>();
        graphDistributeMap.forEachEntry(2, distEntry -> {
            final DistributorKey distKey = distEntry.getKey();
            final DistributorDto distDto = distEntry.getValue();
            final String groupId = StrUtil.blankToDefault(distKey.getGroupId(), "");

            graphMap.putIfAbsent(groupId, new GraphDto());
            GraphDto gm = graphMap.get(groupId);
            gm.addRel(distKey.getMarkerId(), distDto.getRelationId(), distDto.getRelation());
            gm.addPaths(distDto.getPathRefs());
        });

        final ConcurrentHashMap<String, GraphVo> graphData = new ConcurrentHashMap<>();
        graphMap.forEachEntry(2, graphEntry -> {
            graphData.putIfAbsent(graphEntry.getKey(), graphEntry.getValue().toVo());
        });

        return graphData;
    }
    //////////////END:绘图数据方法//////////////

    //////////////START:路线相关数据方法//////////////
    public static List<Long> getPathMarkerIdsFromList(List<MarkerLinkageVo> linkageVos) {
        if(CollUtil.isEmpty(linkageVos)) {
            return new ArrayList<>();
        }
        return linkageVos.parallelStream()
                .filter(Objects::nonNull)
                .map(MarkerLinkageVo::getPath)
                .filter(Objects::nonNull)
                .flatMap(List::stream)
                .map(path -> new Long[]{path.getId1(), path.getId2()})
                .flatMap(Stream::of)
                .filter(id -> id != null && id.compareTo(0L) > 0)
                .distinct()
                .collect(Collectors.toList());
    }

    public static List<Long> getPathMarkerIdsFromGraph(Map<String, GraphVo> linkageMap) {
        if(CollUtil.isEmpty(linkageMap)) {
            return new ArrayList<>();
        }
        return linkageMap.values().parallelStream()
                .filter(Objects::nonNull)
                .map(GraphVo::getPathRefs)
                .map(Map::values)
                .flatMap(Collection::stream)
                .flatMap(List::stream)
                .map(path -> new Long[]{path.getId1(), path.getId2()})
                .flatMap(Stream::of)
                .filter(id -> id != null && id.compareTo(0L) > 0)
                .distinct()
                .collect(Collectors.toList());
    }

    public static void patchPathMarkerCoordsInList(
            List<MarkerLinkageVo> linkageVos,
            Map<Long, Point2D.Double> markerCoords
    ) {
        linkageVos.parallelStream().forEach(linkage -> {
            synchronized (linkage) {
                linkage.getPath().forEach(path -> {
                    final Long id1 = path.getId1();
                    final Point2D.Double coord1 = markerCoords.get(id1);
                    if(coord1 != null) {
                        path.setX1(coord1.getX());
                        path.setY1(coord1.getY());
                    }
                    final Long id2 = path.getId2();
                    final Point2D.Double coord2 = markerCoords.get(id2);
                    if(coord2 != null) {
                        path.setX2(coord2.getX());
                        path.setY2(coord2.getY());
                    }
                });
            }
        });
    }

    public static void patchPathMarkerCoordsInGraph(
            Map<String, GraphVo> linkageMap,
            Map<Long, Point2D.Double> markerCoords
    ) {
        linkageMap.forEach((groupId, graphVo) -> {
            graphVo.getPathRefs().forEach((refId, refPaths) -> {
                synchronized (refPaths) {
                    refPaths.forEach(path -> {
                        final Long id1 = path.getId1();
                        final Point2D.Double coord1 = markerCoords.get(id1);
                        if(coord1 != null) {
                            path.setX1(coord1.getX());
                            path.setY1(coord1.getY());
                        }
                        final Long id2 = path.getId2();
                        final Point2D.Double coord2 = markerCoords.get(id2);
                        if(coord2 != null) {
                            path.setX2(coord2.getX());
                            path.setY2(coord2.getY());
                        }
                    });
                }
            });
        });
    }
    //////////////END:路线相关数据方法//////////////

    //////////////START:关联点位方法//////////////
    public static Map<String, MarkerLinkage> getLinkSearchMap(List<MarkerLinkage> linkageList) {
        return linkageList.parallelStream().collect(Collectors.toConcurrentMap(
            linkageEntity -> MarkerLinkageDataHelper.getIdHash(Arrays.asList(linkageEntity.getFromId(), linkageEntity.getToId())),
            linkageEntity -> linkageEntity,
            (o, n) -> n
        ));
    }

    public static Map<String, MarkerLinkage> patchLinkSearchMap(Map<String, MarkerLinkage> linkageMap, List<MarkerLinkageVo> linkageVos, String groupId) {
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
            final String idHash = MarkerLinkageDataHelper.getIdHash(Arrays.asList(linkageVo.getFromId(), linkageVo.getToId()));
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
    //////////////END:关联点位方法//////////////

}
