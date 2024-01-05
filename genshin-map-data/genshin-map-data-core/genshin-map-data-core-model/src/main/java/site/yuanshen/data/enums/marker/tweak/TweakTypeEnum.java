package site.yuanshen.data.enums.marker.tweak;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import site.yuanshen.data.helper.marker.tweak.MarkerTweakTransformer;
import site.yuanshen.data.vo.MarkerItemLinkVo;
import site.yuanshen.data.vo.adapter.marker.tweak.TweakConfigMetaVo;

import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;

@RequiredArgsConstructor
@Getter
public enum TweakTypeEnum {
    UPDATE(
            "update",
            (v, meta) -> MarkerTweakTransformer.applyUpdate(v, meta)
    ),
    REPLACE(
            "replace",
            (v, meta) -> MarkerTweakTransformer.applyReplace((String) v, meta)
    ),
    REPLACE_REGEX(
            "replaceRegex",
            (v, meta) -> MarkerTweakTransformer.applyReplaceRegex((String) v, meta)
    ),
    PREPEND(
            "prepend",
            (v, meta) -> MarkerTweakTransformer.applyPrepend((String) v, meta)
    ),
    APPEND(
            "append",
            (v, meta) -> MarkerTweakTransformer.applyAppend((String) v, meta)
    ),
    TRIM_LEFT(
            "trimLeft",
            (v, meta) -> MarkerTweakTransformer.applyTrimLeft((String) v, meta)
    ),
    TRIM_RIGHT(
            "trimRight",
            (v, meta) -> MarkerTweakTransformer.applyTrimRight((String) v, meta)
    ),
    REMOVE_LEFT(
            "removeLeft",
            (v, meta) -> MarkerTweakTransformer.applyRemoveLeft((String) v, meta)
    ),
    REMOVE_RIGHT(
            "removeRight",
            (v, meta) -> MarkerTweakTransformer.applyRemoveRight((String) v, meta)
    ),
    MERGE(
            "merge",
            (v, meta) -> MarkerTweakTransformer.applyMerge((Map<String, Object>) v, meta)
    ),
    UPDATE_ITEM_LIST(
            "updateItemList",
            (v, meta) -> MarkerTweakTransformer.applyUpdateItemList((List<MarkerItemLinkVo>) v, meta)
    ),
    INSERT_ITEM_LIST_IF_ABSENT(
            "insertItemListIfAbsent",
            (v, meta) -> MarkerTweakTransformer.applyInsertItemListIfAbsent((List<MarkerItemLinkVo>) v, meta)
    );

    @JsonValue
    private final String name;

    private final BiFunction<?, TweakConfigMetaVo, ?> transformer;
}
