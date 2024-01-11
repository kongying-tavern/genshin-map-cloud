package site.yuanshen.data.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.Version;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import site.yuanshen.data.base.BaseEntity;
import lombok.*;

/**
 * 物品类型表
 *
 * @since 2023-04-23 01:08:53
 */
@Data
@With
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@TableName("item_type")
public class ItemType extends BaseEntity {

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
    private Timestamp updateTime;

    /**
     * 图标标签
     */
    @TableField("icon_tag")
    private String iconTag;

    /**
     * 类型名
     */
    @TableField("name")
    private String name;

    /**
     * 类型补充说明
     */
    @TableField("content")
    private String content;

    /**
     * 父级类型ID（无父级则为-1）
     */
    @TableField("parent_id")
    private Long parentId;

    /**
     * 是否为末端类型
     */
    @TableField("is_final")
    private Boolean isFinal;

    /**
     * 隐藏标记
     */
    @TableField("hidden_flag")
    private Integer hiddenFlag;

    /**
     * 排序
     */
    @TableField("sort_index")
    private Integer sortIndex;

}
