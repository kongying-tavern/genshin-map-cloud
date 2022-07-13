package site.yuanshen.data.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * 物品类型前端封装
 *
 * @author Moment
 * @since 2022-06-15
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
@Schema(title = "ItemType前端封装", description = "物品类型前端封装")
public class ItemTypeVo {

    /**
     * 类型ID
     */
    @Schema(title = "类型ID")
    private Long typeId;

    /**
     * 图标标签
     */
    @Schema(title = "图标标签")
    private String iconTag;

    /**
     * 类型名
     */
    @Schema(title = "类型名")
    private String name;

    /**
     * 类型补充说明
     */
    @Schema(title = "类型补充说明")
    private String content;

    /**
     * 父级类型ID（无父级则为-1）
     */
    @Schema(title = "父级类型ID（无父级则为-1）")
    private Long parentId;

    /**
     * 是否为末端类型
     */
    @Schema(title = "是否为末端类型")
    private Boolean isFinal;

    /**
     * 隐藏标志
     */
    @Schema(title = "隐藏标志")
    private Integer hiddenFlag;

    /**
     * 物品类型排序
     */
    @Schema(title = "物品类型排序")
    private Integer sortIndex;
}
