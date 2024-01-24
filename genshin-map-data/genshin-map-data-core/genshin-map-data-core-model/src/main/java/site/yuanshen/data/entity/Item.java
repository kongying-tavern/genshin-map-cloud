package site.yuanshen.data.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;
import site.yuanshen.data.base.BaseEntity;

import java.sql.Timestamp;

/**
 * 物品表
 *
 * @since 2023-04-23 01:08:53
 */
@Data
@With
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@TableName("item")
public class Item extends BaseEntity {

    /**
     * 乐观锁
     */
    @TableField(value = "version", fill = FieldFill.INSERT_UPDATE)
    @Version
    private Long version;

    /**
     * ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 更新人
     */
    @TableField(value = "updater_id", fill = FieldFill.INSERT_UPDATE)
    private Long updaterId;

    /**
     * 更新时间
     */
    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    @JsonFormat(shape = JsonFormat.Shape.NUMBER_INT)
    private Timestamp updateTime;

    /**
     * 物品名称
     */
    @TableField("name")
    private String name;

    /**
     * 地区ID（须确保是末端地区）
     */
    @TableField("area_id")
    private Long areaId;

    /**
     * 默认刷新时间;单位:毫秒
     */
    @TableField("default_refresh_time")
    private Long defaultRefreshTime;

    /**
     * 默认描述模板;用于提交新物品点位时的描述模板
     */
    @TableField("default_content")
    private String defaultContent;

    /**
     * 默认数量
     */
    @TableField("default_count")
    private Integer defaultCount;

    /**
     * 图标标签
     */
    @TableField("icon_tag")
    private String iconTag;

    /**
     * 图标样式类型
     */
    @TableField("icon_style_type")
    private Integer iconStyleType;

    /**
     * 隐藏标志
     */
    @TableField("hidden_flag")
    private Integer hiddenFlag;

    /**
     * 物品排序
     */
    @TableField("sort_index")
    private Integer sortIndex;

    /**
     * 特殊物品标记;二进制表示；低位第一位：前台是否显示
     */
    @TableField("special_flag")
    private Integer specialFlag;

}
