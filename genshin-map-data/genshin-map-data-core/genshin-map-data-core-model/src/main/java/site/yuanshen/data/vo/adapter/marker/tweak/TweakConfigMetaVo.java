package site.yuanshen.data.vo.adapter.marker.tweak;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import site.yuanshen.data.vo.MarkerItemLinkVo;

import java.util.List;
import java.util.Map;

@Data
@Schema(title = "调整配置数据前端封装")
public class TweakConfigMetaVo {
    @Schema(title = "数据值")
    private Object value;

    @Schema(title = "检查文本")
    private String test;

    @Schema(title = "替换为")
    private String replace;

    @Schema(title = "键值对映射")
    private Map<String, Object> map;

    @Schema(title = "物品关联")
    private List<MarkerItemLinkVo> itemList;
}
