package site.yuanshen.data.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.LocalDateTime;

/**
* 评分统计前端封装
*
* @since 2023-04-22 06:47:07
*/
@Data
@With
@NoArgsConstructor
@AllArgsConstructor
@Schema(title = "ScoreStat前端封装", description = "评分统计前端封装")
public class ScoreStatVo {

    /**
     * 乐观锁
     */
    @Schema(title = "乐观锁")
    private Long version;

    /**
     * ID
     */
    @Schema(title = "ID")
    private Long id;

    /**
     * 更新人
     */
    @Schema(title = "更新人")
    private Long updaterId;

    /**
     * 更新时间
     */
    @Schema(title = "更新时间")
    private LocalDateTime updateTime;

    /**
     * 统计范围
     */
    @Schema(title = "统计范围")
    private String scope;

    /**
     * 统计颗粒度
     */
    @Schema(title = "统计颗粒度")
    private String span;

    /**
     * 统计起始时间
     */
    @Schema(title = "统计起始时间")
    private LocalDateTime spanStartTime;

    /**
     * 统计终止时间
     */
    @Schema(title = "统计终止时间")
    private LocalDateTime spanEndTime;

    /**
     * 用户ID
     */
    @Schema(title = "用户ID")
    private Long userId;

    /**
     * 修改的字段JSON
     */
    @Schema(title = "修改的字段JSON")
    private String content;

}