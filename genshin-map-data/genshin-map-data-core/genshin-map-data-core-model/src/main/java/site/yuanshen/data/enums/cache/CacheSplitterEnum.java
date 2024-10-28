package site.yuanshen.data.enums.cache;

import lombok.AllArgsConstructor;
import lombok.Getter;
import site.yuanshen.data.enums.HiddenFlagEnum;
import site.yuanshen.data.helper.cache.ItemCacheSplitter;
import site.yuanshen.data.helper.cache.MarkerCacheSplitter;
import site.yuanshen.data.vo.ItemVo;
import site.yuanshen.data.vo.MarkerVo;
import site.yuanshen.data.vo.adapter.cache.ItemListCacheKey;
import site.yuanshen.data.vo.adapter.cache.MarkerListCacheKey;

import java.util.List;
import java.util.Map;
import java.util.function.Function;

@AllArgsConstructor
public enum CacheSplitterEnum {
    NORMAL(
        ItemCacheSplitter::splitNormal,
        MarkerCacheSplitter::splitNormal
    ),
    INVISIBLE(
        ItemCacheSplitter::splitInvisible,
        MarkerCacheSplitter::splitInvisible
    ),
    TEST(
        ItemCacheSplitter::splitTest,
        MarkerCacheSplitter::splitTest
    ),
    EASTER_EGG(
        ItemCacheSplitter::splitEasterEgg,
        MarkerCacheSplitter::splitEasterEgg
    );

    @Getter
    private Function<List<ItemVo>, Map<ItemListCacheKey, List<ItemVo>>> itemSplitter;
    @Getter
    private Function<List<MarkerVo>, Map<MarkerListCacheKey, List<MarkerVo>>> markerSplitter;

    public static CacheSplitterEnum findSplitter(HiddenFlagEnum hiddenFlag) {
        CacheSplitterEnum found = null;
        if(hiddenFlag == null)
            return found;

        for (CacheSplitterEnum keygenEnum : CacheSplitterEnum.values()) {
            if(keygenEnum.name().equals(hiddenFlag.name())) {
                found = keygenEnum;
                break;
            }
        }
        return found;
    }
}
