package site.yuanshen.data.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;
import site.yuanshen.data.base.BaseEntity;
import lombok.*;
import lombok.experimental.Accessors;

/**
 * 评分统计
 *
 * @author Alex Fang
 * @since 2023-01-15 10:30:22
 */
@Data
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@TableName("score_stat")
public class ScoreStat extends BaseEntity {

    /**
     * ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

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
    @TableField("content")
    private String content;


}
