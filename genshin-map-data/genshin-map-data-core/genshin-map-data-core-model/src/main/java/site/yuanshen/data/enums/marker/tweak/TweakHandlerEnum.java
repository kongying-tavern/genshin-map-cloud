package site.yuanshen.data.enums.marker.tweak;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Objects;

@RequiredArgsConstructor
@Getter
public enum TweakHandlerEnum {
    // TITLE
    TITLE$$UPDATE(
            "title", "update",
            TweakPropertyEnum.TITLE,
            TweakTypeEnum.UPDATE
    ),
    TITLE$$REPLACE(
            "title", "replace",
            TweakPropertyEnum.TITLE,
            TweakTypeEnum.REPLACE
    ),
    TITLE$$REPLACE_REGEX(
            "title", "replaceRegex",
            TweakPropertyEnum.TITLE,
            TweakTypeEnum.REPLACE_REGEX
    ),
    TITLE$$PREPEND(
            "title", "prepend",
            TweakPropertyEnum.TITLE,
            TweakTypeEnum.PREPEND
    ),
    TITLE$$APPEND(
            "title", "append",
            TweakPropertyEnum.TITLE,
            TweakTypeEnum.APPEND
    ),
    TITLE$$TRIM_LEFT(
            "title", "trimLeft",
            TweakPropertyEnum.TITLE,
            TweakTypeEnum.TRIM_LEFT
    ),
    TITLE$$TRIM_RIGHT(
            "title", "trimRight",
            TweakPropertyEnum.TITLE,
            TweakTypeEnum.TRIM_RIGHT
    ),
    TITLE$$REMOVE_LEFT(
            "title", "removeLeft",
            TweakPropertyEnum.TITLE,
            TweakTypeEnum.REMOVE_LEFT
    ),
    TITLE$$REMOVE_RIGHT(
            "title", "removeRight",
            TweakPropertyEnum.TITLE,
            TweakTypeEnum.REMOVE_RIGHT
    ),
    // CONTENT
    CONTENT$$UPDATE(
            "content", "update",
            TweakPropertyEnum.CONTENT,
            TweakTypeEnum.UPDATE
    ),
    CONTENT$$REPLACE(
            "content", "replace",
            TweakPropertyEnum.CONTENT,
            TweakTypeEnum.REPLACE
    ),
    CONTENT$$REPLACE_REGEX(
            "content", "replaceRegex",
            TweakPropertyEnum.CONTENT,
            TweakTypeEnum.REPLACE_REGEX
    ),
    CONTENT$$PREPEND(
            "content", "prepend",
            TweakPropertyEnum.CONTENT,
            TweakTypeEnum.PREPEND
    ),
    CONTENT$$APPEND(
            "content", "append",
            TweakPropertyEnum.CONTENT,
            TweakTypeEnum.APPEND
    ),
    CONTENT$$TRIM_LEFT(
            "content", "trimLeft",
            TweakPropertyEnum.CONTENT,
            TweakTypeEnum.TRIM_LEFT
    ),
    CONTENT$$TRIM_RIGHT(
            "content", "trimRight",
            TweakPropertyEnum.CONTENT,
            TweakTypeEnum.TRIM_RIGHT
    ),
    CONTENT$$REMOVE_LEFT(
            "content", "removeLeft",
            TweakPropertyEnum.CONTENT,
            TweakTypeEnum.REMOVE_LEFT
    ),
    CONTENT$$REMOVE_RIGHT(
            "content", "removeRight",
            TweakPropertyEnum.CONTENT,
            TweakTypeEnum.REMOVE_RIGHT
    ),
    // REFRESH_TIME
    REFRESH_TIME$$UPDATE(
            "refreshTime", "update",
            TweakPropertyEnum.REFRESH_TIME,
            TweakTypeEnum.UPDATE
    ),
    // HIDDEN_FLAG
    HIDDEN_FLAG$$UPDATE(
            "hiddenFlag", "update",
            TweakPropertyEnum.HIDDEN_FLAG,
            TweakTypeEnum.UPDATE
    ),
    // VIDEO_PATH
    VIDEO_PATH$$UPDATE(
            "videoPath", "update",
            TweakPropertyEnum.VIDEO_PATH,
            TweakTypeEnum.UPDATE
    ),
    // EXTRA
    EXTRA$$MERGE(
            "extra", "merge",
            TweakPropertyEnum.EXTRA,
            TweakTypeEnum.MERGE
    ),
    // ITEM_LIST
    ITEM_LIST$$UPDATE(
            "itemList", "update",
            TweakPropertyEnum.ITEM_LIST,
            TweakTypeEnum.UPDATE_ITEM_LIST
    ),
    ITEM_LIST$$INSERT_IF_ABSENT(
            "itemList", "insertIfAbsent",
            TweakPropertyEnum.ITEM_LIST,
            TweakTypeEnum.INSERT_ITEM_LIST_IF_ABSENT
    ),
    ITEM_LIST$$INSERT_OR_UPDATE(
            "itemList", "insertOrUpdate",
            TweakPropertyEnum.ITEM_LIST,
            TweakTypeEnum.INSERT_ITEM_LIST_OR_UPDATE
    ),
    ITEM_LIST$$REMOVE_ITEM(
            "itemList", "removeItem",
            TweakPropertyEnum.ITEM_LIST,
            TweakTypeEnum.REMOVE_ITEM_LIST_ITEM
    ),
    ITEM_LIST$$PRESERVE_ITEM(
            "itemList", "preserveItem",
            TweakPropertyEnum.ITEM_LIST,
            TweakTypeEnum.PRESERVE_ITEM_LIST_ITEM
    );

    private final String propName;
    private final String typeName;
    private final TweakPropertyEnum prop;
    private final TweakTypeEnum type;

    public static TweakHandlerEnum find(String propName, String typeName) {
        for(TweakHandlerEnum handler : TweakHandlerEnum.values()) {
            final String handlerProp = handler.propName;
            final String handlerType = handler.typeName;
            if(Objects.equals(handlerProp, propName) && Objects.equals(handlerType, typeName)) {
                return handler;
            }
        }
        return null;
    }
}
