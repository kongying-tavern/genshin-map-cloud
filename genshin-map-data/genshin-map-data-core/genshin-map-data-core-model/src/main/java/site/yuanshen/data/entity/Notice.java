package site.yuanshen.data.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.*;
import site.yuanshen.data.base.BaseEntity;
import site.yuanshen.handler.MBPJsonArrayTypeHandler;

import java.sql.Timestamp;
import java.util.List;

/**
 * 消息通知
 *
 * @since 2023-05-31 03:12:05
 */
@Data
@With
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@TableName(value = "notice", autoResultMap = true)
public class Notice extends BaseEntity {

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
     * 频道
     */
    @TableField(value = "channel", typeHandler = MBPJsonArrayTypeHandler.class)
    private List<String> channel;

    /**
     * 标题
     */
    @TableField("title")
    private String title;

    /**
     * 内容
     */
    @TableField("content")
    private String content;

    /**
     * 有效期开始时间
     */
    @TableField("valid_time_start")
    private Timestamp validTimeStart;

    /**
     * 有效期结束时间
     */
    @TableField("valid_time_end")
    private Timestamp validTimeEnd;

}
