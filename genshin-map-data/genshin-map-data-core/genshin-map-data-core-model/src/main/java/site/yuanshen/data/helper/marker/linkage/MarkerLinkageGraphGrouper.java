package site.yuanshen.data.helper.marker.linkage;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import site.yuanshen.data.dto.adapter.marker.linkage.graph.AccumulatorCache;
import site.yuanshen.data.dto.adapter.marker.linkage.graph.LinkRefDto;
import site.yuanshen.data.vo.adapter.marker.linkage.PathEdgeVo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MarkerLinkageGraphGrouper {
    public static List<AccumulatorCache> withTrigger(List<AccumulatorCache> caches) {
        return caches;
    }

    public static List<AccumulatorCache> withTriggerAll(List<AccumulatorCache> caches) {
        return groupByTriggers(caches);
    }

    public static List<AccumulatorCache> withTriggerAny(List<AccumulatorCache> caches) {
        return groupByTriggers(caches);
    }

    public static List<AccumulatorCache> withRelated(List<AccumulatorCache> caches) {
        return caches;
    }

    public static List<AccumulatorCache> withEquivalent(List<AccumulatorCache> caches) {
        return caches;
    }

    // 辅助方法
    private static List<AccumulatorCache> groupByTriggers(List<AccumulatorCache> caches) {
        final Map<String, String> keyMap = new HashMap<>();
        final Map<String, AccumulatorCache> cacheMap = new HashMap<>();

        for (AccumulatorCache cache : caches) {
            final String cacheId = cache.getCacheId();
            if(StrUtil.isBlank(cacheId)) continue;

            // 计算起始点位 ID Hash
            final List<Long> fromIds = new ArrayList<>();
            for (LinkRefDto linkage : cache.getLinkageSet()) {
                final Long fromId = linkage.getFromId();
                if(fromId == null) continue;
                fromIds.add(fromId);
            }
            if(CollUtil.isEmpty(fromIds)) continue;
            final String fromIdHash = MarkerLinkageDataHelper.getIdHash(fromIds);

            // 插入目标点位映射
            final String updateCacheId = StrUtil.blankToDefault(keyMap.get(fromIdHash), cacheId);
            final AccumulatorCache updateCache = cacheMap.containsKey(updateCacheId) ? cacheMap.get(updateCacheId) : new AccumulatorCache();
            for (LinkRefDto linkage : cache.getLinkageSet()) {
                updateCache.addLinkage(linkage);
            }
            for (Map.Entry<Long, List<PathEdgeVo>> path : cache.getPathMap().entrySet()) {
                updateCache.addPath(path.getKey(), path.getValue());
            }

            keyMap.put(fromIdHash, updateCacheId);
            cacheMap.put(updateCacheId, updateCache);
        }

        return new ArrayList<>(cacheMap.values());
    }
}
