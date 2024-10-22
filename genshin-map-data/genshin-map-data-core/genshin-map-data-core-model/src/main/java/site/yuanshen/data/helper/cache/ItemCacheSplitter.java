package site.yuanshen.data.helper.cache;

import site.yuanshen.data.vo.ItemVo;
import site.yuanshen.data.vo.adapter.cache.ItemListCacheKey;

import java.util.List;
import java.util.Map;

public class ItemCacheSplitter {
    public static Map<ItemListCacheKey, List<ItemVo>> splitNormal(List<ItemVo> itemList) {
        return Map.of((new ItemListCacheKey()).withIndex(0), itemList);
    }

    public static Map<ItemListCacheKey, List<ItemVo>> splitInvisible(List<ItemVo> itemList) {
        return Map.of((new ItemListCacheKey()).withIndex(0), itemList);
    }

    public static Map<ItemListCacheKey, List<ItemVo>> splitTest(List<ItemVo> itemList) {
        return Map.of((new ItemListCacheKey()).withIndex(0), itemList);
    }

    public static Map<ItemListCacheKey, List<ItemVo>> splitEasterEgg(List<ItemVo> itemList) {
        return Map.of((new ItemListCacheKey()).withIndex(0), itemList);
    }
}
