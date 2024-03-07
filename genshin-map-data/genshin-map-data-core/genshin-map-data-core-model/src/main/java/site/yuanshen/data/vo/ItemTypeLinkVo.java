package site.yuanshen.data.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.With;

/**
* 物品-类型关联表前端封装
*
* @since 2023-04-22 06:47:07
*/
@Data
@With
@NoArgsConstructor
@AllArgsConstructor
@Schema(title = "ItemTypeLink前端封装", description = "物品-类型关联表前端封装")
public class ItemTypeLinkVo {

    /**
     * 类型ID;此处必须为末端类型
     */
    @Schema(title = "类型ID;此处必须为末端类型")
    private Long typeId;

    /**
     * 物品ID
     */
    @Schema(title = "物品ID")
    private Long itemId;

}
