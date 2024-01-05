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
            "title", String.class,
            MarkerDto::getMarkerTitle,
            (marker, data) -> marker.setMarkerTitle((String) data)
    ),
    CONTENT(
            "content", String.class,
            MarkerDto::getContent,
            (marker, data) -> marker.setContent((String) data)
    ),
    REFRESH_TIME(
            "refreshTime", Long.class,
            MarkerDto::getRefreshTime,
            (marker, data) -> marker.setRefreshTime(NumberUtil.parseLong(data.toString()))
    ),
    HIDDEN_FLAG(
            "hiddenFlag", Integer.class,
            MarkerDto::getHiddenFlag,
            (marker, data) -> marker.setHiddenFlag(NumberUtil.parseInt(data.toString()))
    ),
    EXTRA(
            "extra", Map.class,
            MarkerDto::getExtra,
            (marker, data) -> marker.setExtra((Map<String, Object>) data)
    ),
    ITEM_LIST(
            "itemList", List.class,
            MarkerDto::getItemList,
            (markerDto, data) -> markerDto.setItemList((List<MarkerItemLinkVo>) data)
    );

    @JsonValue
    private final String name;

    private final Class<?> propClass;
    private final Function<MarkerDto, ?> getter;
    private final BiConsumer<MarkerDto, ?> setter;
}
