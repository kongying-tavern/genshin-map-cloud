package site.yuanshen.data.enums.marker.tweak;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Objects;

@RequiredArgsConstructor
@Getter
public enum TweakHandlerEnum {
    // TITLE
    TITLE$$UPDATE(
            TweakPropertyEnum.TITLE,
            TweakTypeEnum.UPDATE
    ),
    TITLE$$REPLACE(
            TweakPropertyEnum.TITLE,
            TweakTypeEnum.REPLACE
    ),
    TITLE$$REPLACE_REGEX(
            TweakPropertyEnum.TITLE,
            TweakTypeEnum.REPLACE_REGEX
    ),
    TITLE$$PREPEND(
            TweakPropertyEnum.TITLE,
            TweakTypeEnum.PREPEND
    ),
    TITLE$$APPEND(
            TweakPropertyEnum.TITLE,
            TweakTypeEnum.APPEND
    ),
    TITLE$$TRIM_LEFT(
            TweakPropertyEnum.TITLE,
            TweakTypeEnum.TRIM_LEFT
    ),
    TITLE$$TRIM_RIGHT(
            TweakPropertyEnum.TITLE,
            TweakTypeEnum.TRIM_RIGHT
    ),
    TITLE$$REMOVE_LEFT(
            TweakPropertyEnum.TITLE,
            TweakTypeEnum.REMOVE_LEFT
    ),
    TITLE$$REMOVE_RIGHT(
            TweakPropertyEnum.TITLE,
            TweakTypeEnum.REMOVE_RIGHT
    ),
    // CONTENT
    CONTENT$$UPDATE(
            TweakPropertyEnum.CONTENT,
            TweakTypeEnum.UPDATE
    ),
    CONTENT$$REPLACE(
            TweakPropertyEnum.CONTENT,
            TweakTypeEnum.REPLACE
    ),
    CONTENT$$REPLACE_REGEX(
            TweakPropertyEnum.CONTENT,
            TweakTypeEnum.REPLACE_REGEX
    ),
    CONTENT$$PREPEND(
            TweakPropertyEnum.CONTENT,
            TweakTypeEnum.PREPEND
    ),
    CONTENT$$APPEND(
            TweakPropertyEnum.CONTENT,
            TweakTypeEnum.APPEND
    ),
    CONTENT$$TRIM_LEFT(
            TweakPropertyEnum.CONTENT,
            TweakTypeEnum.TRIM_LEFT
    ),
    CONTENT$$TRIM_RIGHT(
            TweakPropertyEnum.CONTENT,
            TweakTypeEnum.TRIM_RIGHT
    ),
    CONTENT$$REMOVE_LEFT(
            TweakPropertyEnum.CONTENT,
            TweakTypeEnum.REMOVE_LEFT
    ),
    CONTENT$$REMOVE_RIGHT(
            TweakPropertyEnum.CONTENT,
            TweakTypeEnum.REMOVE_RIGHT
    ),
    // REFRESH_TIME
    REFRESH_TIME$$UPDATE(
            TweakPropertyEnum.REFRESH_TIME,
            TweakTypeEnum.UPDATE
    ),
    // HIDDEN_FLAG
    HIDDEN_FLAG$$UPDATE(
            TweakPropertyEnum.HIDDEN_FLAG,
            TweakTypeEnum.UPDATE
    ),
    // UNDERGROUND
    UNDERGROUND$$MERGE(
            TweakPropertyEnum.UNDERGROUND,
            TweakTypeEnum.MERGE
    ),
    // ITEM_LIST
    ITEM_LIST$$UPDATE(
            TweakPropertyEnum.ITEM_LIST,
            TweakTypeEnum.UPDATE
    );

    private final TweakPropertyEnum prop;
    private final TweakTypeEnum type;

    public static TweakHandlerEnum find(TweakPropertyEnum prop, TweakTypeEnum type) {
        for(TweakHandlerEnum handler : TweakHandlerEnum.values()) {
            final TweakPropertyEnum handlerProp = handler.prop;
            TweakTypeEnum handlerType = handler.type;
            if(Objects.equals(handlerProp, prop) && Objects.equals(handlerType, type)) {
                return handler;
            }
        }
        return null;
    }
}
