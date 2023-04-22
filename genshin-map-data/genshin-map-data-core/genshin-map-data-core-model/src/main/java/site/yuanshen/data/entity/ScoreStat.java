package site.yuanshen.data.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.Version;
import java.time.LocalDateTime;
import site.yuanshen.data.base.BaseEntity;
import lombok.*;

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
@TableName("score_stat")
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
    private LocalDateTime updateTime;

    /**
     * 统计范围
     */
    @TableId("scope")
    private String scope;

    /**
     * 统计颗粒度
     */
    @TableId("span")
    private String span;

    /**
     * 统计起始时间
     */
    @TableId("span_start_time")
    private LocalDateTime spanStartTime;

    /**
     * 统计终止时间
     */
    @TableId("span_end_time")
    private LocalDateTime spanEndTime;

    /**
     * 用户ID
     */
    @TableId("user_id")
    private Long userId;

    /**
     * 修改的字段JSON
     */
    @TableField("content")
    private String content;

}
