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
 * 地区主表
 *
 * @author Moment
 * @since 2022-06-25 12:17:27
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("area")
@Schema(title = "Area对象", description = "地区主表")
public class Area extends BaseEntity {

    /**
     * id
     */
    @Schema(title = "id")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

//	/**
//	 * 地区id
//	 */
//	@Schema(title = "地区id")
//	@TableField("area_id")
//	private Long areaId;

    /**
     * 地区名称
     */
    @Schema(title = "地区名称")
    @TableField("name")
    private String name;

    /**
     * 地区说明
     */
    @Schema(title = "地区说明")
    @TableField("content")
    private String content;

    /**
     * 图标标签
     */
    @Schema(title = "图标标签")
    @TableField("icon_tag")
    private String iconTag;

    /**
     * 父级地区id（无父级则为-1）
     */
    @Schema(title = "父级地区id（无父级则为-1）")
    @TableField("parent_id")
    private Long parentId;

    /**
     * 是否为末端地区
     */
    @Schema(title = "是否为末端地区")
    @TableField("is_final")
    private Boolean isFinal;

    /**
     * 隐藏标志
     */
    @Schema(title = "隐藏标志")
    @TableField("hidden_flag")
    private Integer hiddenFlag;

    /**
     * 地区排序
     */
    @Schema(title = "地区排序")
    @TableField("sort_index")
    private Integer sortIndex;

}
