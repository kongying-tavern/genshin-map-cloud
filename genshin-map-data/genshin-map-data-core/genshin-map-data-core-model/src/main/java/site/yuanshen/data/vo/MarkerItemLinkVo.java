package site.yuanshen.data.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;


/**
* 点位-物品关联表前端封装
*
* @since 2023-04-22 06:47:07
*/
@Data
@With
@NoArgsConstructor
@AllArgsConstructor
@Schema(title = "MarkerItemLink前端封装", description = "点位-物品关联表前端封装")
public class MarkerItemLinkVo {

    /**
     * 物品ID
     */
    @Schema(title = "物品ID")
    private Long itemId;

    /**
     * 物品于该点位数量
     */
    @Schema(title = "物品于该点位数量")
    private Integer count;

    /**
     * 图标标签
     */
    @Schema(title = "图标标签")
    private String iconTag;

}