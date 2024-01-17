package site.yuanshen.data.enums.marker.tweak;

import cn.hutool.core.util.NumberUtil;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import site.yuanshen.data.dto.MarkerDto;
import site.yuanshen.data.vo.MarkerItemLinkVo;

import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Function;

@RequiredArgsConstructor
@Getter
public enum TweakPropertyEnum {
    TITLE(
            "title",
            MarkerDto::getMarkerTitle,
            (marker, data) -> marker.setMarkerTitle((String) data)
    ),
    CONTENT(
            "content",
            MarkerDto::getContent,
            (marker, data) -> marker.setContent((String) data)
    ),
    REFRESH_TIME(
            "refreshTime",
            MarkerDto::getRefreshTime,
            (marker, data) -> marker.setRefreshTime(NumberUtil.parseLong(data.toString()))
    ),
    HIDDEN_FLAG(
            "hiddenFlag",
            MarkerDto::getHiddenFlag,
            (marker, data) -> marker.setHiddenFlag(NumberUtil.parseInt(data.toString()))
    ),
    EXTRA(
            "extra",
            MarkerDto::getExtra,
            (marker, data) -> marker.setExtra((Map<String, Object>) data)
    ),
    ITEM_LIST(
            "itemList",
            MarkerDto::getItemList,
            (marker, data) -> marker.setItemList((List<MarkerItemLinkVo>) data)
    );

    @JsonValue
    private final String name;

    private final Function<MarkerDto, ?> getter;
    private final BiConsumer<MarkerDto, ?> setter;
}
