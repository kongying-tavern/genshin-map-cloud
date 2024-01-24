package site.yuanshen.data.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;
import site.yuanshen.data.base.BaseEntity;
import site.yuanshen.handler.MBPJsonObjectTypeHandler;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * 评分统计
 *
 * @since 2023-04-23 01:08:53
 */
@Data
@With
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@TableName(value = "score_stat", autoResultMap = true)
public class ScoreStat extends BaseEntity {

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
     * 统计范围
     */
    @TableField("scope")
    private String scope;

    /**
     * 统计颗粒度
     */
    @TableField("span")
    private String span;

    /**
     * 统计起始时间
     */
    @TableField("span_start_time")
    private LocalDateTime spanStartTime;

    /**
     * 统计终止时间
     */
    @TableField("span_end_time")
    private LocalDateTime spanEndTime;

    /**
     * 用户ID
     */
    @TableField("user_id")
    private Long userId;

    /**
     * 修改的字段JSON
     */
    @TableField(value = "content", typeHandler = MBPJsonObjectTypeHandler.class)
    private Map<String, Object> content;

}
