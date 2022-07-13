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
 * 物品表
 *
 * @author Moment
 * @since 2022-06-25 12:17:27
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("item")
@Schema(title = "Item对象", description = "物品表")
public class Item extends BaseEntity {

    /**
     * id
     */
    @Schema(title = "id")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

//	/**
//	 * 物品id
//	 */
//	@Schema(title = "物品id")
//	@TableField("item_id")
//	private Long itemId;

    /**
     * 物品名称
     */
    @Schema(title = "物品名称")
    @TableField("name")
    private String name;

    /**
     * 地区id（须确保是末端地区）
     */
    @Schema(title = "地区id（须确保是末端地区）")
    @TableField("area_id")
    private Long areaId;

    /**
     * 默认描述模板;用于提交新物品点位时的描述模板
     */
    @Schema(title = "默认描述模板;用于提交新物品点位时的描述模板")
    @TableField("default_content")
    private String defaultContent;

    /**
     * 图标标签
     */
    @Schema(title = "图标标签")
    @TableField("icon_tag")
    private String iconTag;

    /**
     * 图标样式类型
     */
    @Schema(title = "图标样式类型")
    @TableField("icon_style_type")
    private Integer iconStyleType;

    /**
     * 隐藏标志
     */
    @Schema(title = "隐藏标志")
    @TableField("hidden_flag")
    private Integer hiddenFlag;

    /**
     * 刷新时间
     */
    @Schema(title = "刷新时间(单位:毫秒)")
    @TableField("default_refresh_time")
    private Long defaultRefreshTime;

    /**
     * 物品排序
     */
    @Schema(title = "物品排序")
    @TableField("sort_index")
    private Integer sortIndex;

    /**
     * 默认物品数量
     */
    @Schema(title = "默认物品数量")
    @TableField("default_count")
    private Integer defaultCount;

}
