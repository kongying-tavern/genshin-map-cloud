package site.yuanshen.data.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import site.yuanshen.data.base.BaseEntity;

/**
 * 物品类型表
 *
 * @author Moment
 * @since 2022-06-25 12:17:27
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("item_type")
@Schema(title = "ItemType对象", description = "物品类型表")
public class ItemType extends BaseEntity {

    /**
     * id
     */
    @Schema(title = "id")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

//	/**
//	 * 类型id
//	 */
//	@Schema(title = "类型id")
//	@TableField("type_id")
//	private Long typeId;

    /**
     * 图标标签
     */
    @Schema(title = "图标标签")
    @TableField("icon_tag")
    private String iconTag;

    /**
     * 类型名
     */
    @Schema(title = "类型名")
    @TableField("name")
    private String name;

    /**
     * 类型补充说明
     */
    @Schema(title = "类型补充说明")
    @TableField("content")
    private String content;

    /**
     * 父级类型id（无父级则为-1）
     */
    @Schema(title = "父级类型id（无父级则为-1）")
    @TableField("parent_id")
    private Long parentId;

    /**
     * 是否为末端类型
     */
    @Schema(title = "是否为末端类型")
    @TableField("is_final")
    private Boolean isFinal;

    /**
     * 隐藏标志
     */
    @Schema(title = "隐藏标志")
    @TableField("hidden_flag")
    private Integer hiddenFlag;

    /**
     * 物品类型排序
     */
    @Schema(title = "物品类型排序")
    @TableField("sort_index")
    private Integer sortIndex;
}
