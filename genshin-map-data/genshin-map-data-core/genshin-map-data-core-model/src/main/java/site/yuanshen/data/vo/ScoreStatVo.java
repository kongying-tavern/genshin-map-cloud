package site.yuanshen.data.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

/**
 * 评分统计前端封装
 *
 * @author Alex Fang
 * @since 2023-01-15 10:30:22
 */
@Data
@ToString
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
@Schema(title = "ScoreStat前端封装", description = "评分统计前端封装")
public class ScoreStatVo {

    /**
     * ID
     */
    @Schema(title = "ID")
    private Long id;

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
