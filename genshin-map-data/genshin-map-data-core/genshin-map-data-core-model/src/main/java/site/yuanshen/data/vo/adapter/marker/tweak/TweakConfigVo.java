package site.yuanshen.data.vo.adapter.marker.tweak;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import site.yuanshen.data.enums.marker.tweak.TweakPropertyEnum;
import site.yuanshen.data.enums.marker.tweak.TweakTypeEnum;

@Data
@Schema(title = "点位调整配置项前端封装")
public class TweakConfigVo {
    @Schema(title = "需调整的点位属性")
    private String prop;

    @Schema(title = "调整方法类型")
    private String type;

    @Schema(title = "调整配置数据", description = "此项根据调整方法类型使用不同的数据字段")
    private TweakConfigMetaVo meta = new TweakConfigMetaVo();
}
