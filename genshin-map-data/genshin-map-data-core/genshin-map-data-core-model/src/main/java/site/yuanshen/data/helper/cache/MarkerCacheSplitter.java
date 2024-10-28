package site.yuanshen.data.helper.cache;

import site.yuanshen.data.vo.MarkerVo;
import site.yuanshen.data.vo.adapter.cache.MarkerListCacheKey;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class MarkerCacheSplitter {
    public static Map<MarkerListCacheKey, List<MarkerVo>> splitNormal(List<MarkerVo> markerList) {
        final Map<MarkerListCacheKey, List<MarkerVo>> result = new LinkedHashMap<>();
        for (MarkerVo marker : markerList) {
            final int index = (int)(marker.getId() / 3000L);
            final MarkerListCacheKey indexKey = (new MarkerListCacheKey()).withIndex(index);
            result.putIfAbsent(indexKey, new ArrayList<>());
            result.get(indexKey).add(marker);
        }
        return result;
    }

    public static Map<MarkerListCacheKey, List<MarkerVo>> splitInvisible(List<MarkerVo> markerList) {
        return Map.of((new MarkerListCacheKey()).withIndex(0), markerList);
    }

    public static Map<MarkerListCacheKey, List<MarkerVo>> splitTest(List<MarkerVo> markerList) {
        return Map.of((new MarkerListCacheKey()).withIndex(0), markerList);
    }

    public static Map<MarkerListCacheKey, List<MarkerVo>> splitEasterEgg(List<MarkerVo> markerList) {
        return Map.of((new MarkerListCacheKey()).withIndex(0), markerList);
    }
}
