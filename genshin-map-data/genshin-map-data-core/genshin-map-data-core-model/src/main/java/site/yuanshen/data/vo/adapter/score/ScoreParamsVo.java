package site.yuanshen.data.vo.adapter.score;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import site.yuanshen.data.dto.adapter.score.ScoreSpanConfigDto;
import site.yuanshen.data.enums.ScoreSpanEnum;

import java.sql.Timestamp;

/**
 * 评分参数
 *
 * @author Alex Fang
 * @since 2023-01-15
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
@Schema(title = "评分参数", description = "评分参数")
public class ScoreParamsVo {
    /**
     * 统计范围
     */
    @Schema(title = "统计范围")
    private String scope;

    /**
     * 开始时间
     */
    @Schema(title = "开始时间")
    private Timestamp startTime;

    /**
     * 结束时间
     */
    @Schema(title = "结束时间")
    private Timestamp endTime;

    /**
     * 统计颗粒度
     */
    @Schema(title = "统计颗粒度")
    private ScoreSpanEnum span;

    /**
     * 创建人
     */
    private Long generatorId;

    public ScoreSpanConfigDto calculateSpan() {
        return ScoreSpanConfigDto.create()
                .setStartTime(this.startTime)
                .setStartTimeInclude(true)
                .setEndTime(this.endTime)
                .setEndTimeInclude(true)
                .setSpan(this.span)
                .calculateSpan();
    }
}
