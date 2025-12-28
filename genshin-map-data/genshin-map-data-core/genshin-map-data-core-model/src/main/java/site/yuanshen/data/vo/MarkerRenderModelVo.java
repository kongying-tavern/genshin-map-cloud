package site.yuanshen.data.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.With;

import java.util.List;

@Data
@With
@NoArgsConstructor
@AllArgsConstructor
@Schema(title = "Marker前端渲染封装", description = "点位主表前端渲染封装")
public class MarkerRenderModelVo {
    @Schema(title = "乐观锁")
    private Long version;

    @Schema(title = "ID")
    private Long id;

    @Schema(title = "点位名称")
    private String markerTitle;

    @Schema(title = "点位坐标")
    private String position;

    @Schema(title = "点位物品列表")
    private List<MarkerItemLinkVo> itemList;

    @Schema(title = "点位关联组ID")
    private String linkageId;
}
