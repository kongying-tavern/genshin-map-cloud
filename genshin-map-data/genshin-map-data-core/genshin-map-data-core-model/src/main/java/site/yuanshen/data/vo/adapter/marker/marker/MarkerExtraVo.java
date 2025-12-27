package site.yuanshen.data.vo.adapter.marker.marker;

import com.alibaba.fastjson2.JSONWriter;
import com.alibaba.fastjson2.annotation.JSONField;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
@Schema(title = "点位附加数据前端封装")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class MarkerExtraVo {
    @Data
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class Underground {
        @Schema(title = "是否是分层层级")
        @JSONField(name = "is_underground")
        @JsonProperty("is_underground")
        private Boolean isUnderground;

        @Schema(title = "是否是非地面层级")
        @JSONField(name = "is_global")
        @JsonProperty("is_global")
        private Boolean isGlobal;

        @Schema(title = "分层区域", description = "分层区域标签")
        @JSONField(name = "region_levels")
        @JsonProperty("region_levels")
        private List<String> regionLevels;
    }

    @Data
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class IconOverride {
        @Schema(title = "图标ID")
        @JSONField(name = "id")
        @JsonProperty("id")
        private Long id;

        @Schema(title = "最小可见缩放级别", description = "最小可见缩放级别，大于该值可见")
        @JSONField(name = "minZoom", serializeFeatures = JSONWriter.Feature.WriteBigDecimalAsPlain)
        @JsonProperty("minZoom")
        @JsonFormat(shape = JsonFormat.Shape.NUMBER_FLOAT)
        private BigDecimal minZoom;

        @Schema(title = "最大可见缩放级别", description = "最大可见缩放级别，小于该值可见")
        @JSONField(name = "maxZoom", serializeFeatures = JSONWriter.Feature.WriteBigDecimalAsPlain)
        @JsonProperty("maxZoom")
        @JsonFormat(shape = JsonFormat.Shape.NUMBER_FLOAT)
        private BigDecimal maxZoom;
    }

    @Data
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class V2_8_Island {
        @Schema(title = "海岛名")
        @JSONField(name = "island_name")
        @JsonProperty("island_name")
        private String islandName;

        @Schema(title = "海岛状态", description = "海岛状态标签")
        @JSONField(name = "island_state")
        @JsonProperty("island_state")
        private List<String> islandState;
    }

    @Schema(title = "分层层级数据")
    @JSONField(name = "underground")
    @JsonProperty("underground")
    private Underground underground;

    @Schema(title = "图标覆盖数据")
    @JSONField(name = "iconOverride")
    @JsonProperty("iconOverride")
    private IconOverride iconOverride;

    @Schema(title = "1.6 海岛数据", description = "海岛阶段数组")
    @JSONField(name = "1_6_island")
    @JsonProperty("1_6_island")
    private List<String> v1_6_Island;

    @Schema(title = "2.8 海岛数据")
    @JSONField(name = "2_8_island")
    @JsonProperty("2_8_island")
    private V2_8_Island v2_8_Island;
}
