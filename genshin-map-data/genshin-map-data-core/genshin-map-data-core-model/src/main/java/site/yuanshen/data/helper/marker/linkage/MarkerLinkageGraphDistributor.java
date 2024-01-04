package site.yuanshen.data.helper.marker.linkage;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.ConcurrentHashSet;
import cn.hutool.core.util.StrUtil;
import site.yuanshen.data.dto.adapter.marker.linkage.graph.*;

import java.util.Map;
import java.util.Set;

public final class MarkerLinkageGraphDistributor {
    public static void withTrigger(Map<DistributorKey, DistributorDto> groups, AccumulatorKey accumulatorKey, AccumulatorCache cache) {
        addDistribution(groups, accumulatorKey, cache);
    }

    public static void withTriggerAll(Map<DistributorKey, DistributorDto> groups, AccumulatorKey accumulatorKey, AccumulatorCache cache) {
        addDistribution(groups, accumulatorKey, cache);
    }

    public static void withTriggerAny(Map<DistributorKey, DistributorDto> groups, AccumulatorKey accumulatorKey, AccumulatorCache cache) {
        addDistribution(groups, accumulatorKey, cache);
    }

    public static void withRelated(Map<DistributorKey, DistributorDto> groups, AccumulatorKey accumulatorKey, AccumulatorCache cache) {
        addDistribution(groups, accumulatorKey, cache);
    }

    public static void withEquivalent(Map<DistributorKey, DistributorDto> groups, AccumulatorKey accumulatorKey, AccumulatorCache cache) {
        addDistribution(groups, accumulatorKey, cache);
    }

    // 辅助方法
    private static void addDistribution(
            Map<DistributorKey, DistributorDto> groups,
            AccumulatorKey accumulatorKey,
            AccumulatorCache cache
    ) {
        final String cacheId = StrUtil.blankToDefault(cache.getCacheId(), "");
        final Set<LinkRefDto> cacheLinks = CollUtil.defaultIfEmpty(cache.getLinkageSet(), new ConcurrentHashSet<>());
        final RelationDto relation = MarkerLinkageDataHelper.getRelationGroup(cacheLinks, accumulatorKey.getLinkAction());
        DistributorKey distKey = null;

        for (LinkRefDto cacheLink : cacheLinks) {
            // Add distributor keyed by `fromId`
            distKey = MarkerLinkageDataHelper.getDistributeKey(accumulatorKey, cacheId, cacheLink.getFromId());
            groups.computeIfAbsent(distKey, v -> {
                final DistributorDto distDto = new DistributorDto();
                distDto.setRelationId(cacheId);
                distDto.setRelation(relation);
                distDto.addPaths(cache.getPathMap());
                return distDto;
            });

            // Add distributor keyed by `toId`
            distKey = MarkerLinkageDataHelper.getDistributeKey(accumulatorKey, cacheId, cacheLink.getToId());
            groups.computeIfAbsent(distKey, v -> {
                final DistributorDto distDto = new DistributorDto();
                distDto.setRelationId(cacheId);
                distDto.setRelation(relation);
                distDto.addPaths(cache.getPathMap());
                return distDto;
            });
        }
    }
}
