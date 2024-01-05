package site.yuanshen.data.vo.adapter.marker.tweak;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Data
@Schema(title = "点位调整前端封装")
public class TweakVo {
    @Schema(title = "点位ID")
    private List<Long> markerIds;

    @Schema(title = "点位数据调整配置")
    private List<TweakConfigVo> tweaks;
}
