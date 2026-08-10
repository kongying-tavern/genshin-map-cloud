package site.yuanshen.data.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;
import site.yuanshen.data.base.BaseEntity;
import site.yuanshen.handler.MBPJsonObjectTypeHandler;

import java.sql.Timestamp;
import java.util.Map;

/**
 * 图标主表
 *
 * @since 2023-04-23 01:08:53
 */
@Data
@With
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@TableName(value = "icon", autoResultMap = true)
public class Icon extends BaseEntity {

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
     * 图标标签
     */
    @TableField("tag")
    private String tag;

    /**
     * 图标url
     */
    @TableField("url")
    private String url;

    /**
     * 图标变体url
     */
    @TableField(value = "url_variants", typeHandler = MBPJsonObjectTypeHandler.class)
    private Map<String, String> urlVariants;

    /**
     * 图标描述
     */
    @TableField("description")
    private String description;

}
