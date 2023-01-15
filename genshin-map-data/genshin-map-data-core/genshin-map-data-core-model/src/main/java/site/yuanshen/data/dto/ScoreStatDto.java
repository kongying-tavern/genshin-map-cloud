package site.yuanshen.data.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import com.alibaba.fastjson.annotation.JSONField;
import site.yuanshen.common.core.utils.BeanUtils;
import site.yuanshen.data.entity.ScoreStat;
import site.yuanshen.data.vo.ScoreStatVo;

import java.time.LocalDateTime;


/**
 * 评分统计路数据封装
 *
 * @author Alex Fang
 * @since 2023-01-15 10:30:22
 */
@Data
@ToString
@NoArgsConstructor
@AllArgsConstructor
@With
@EqualsAndHashCode
@Schema(title = "ScoreStat数据封装", description = "评分统计数据封装")
public class ScoreStatDto {

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



    public ScoreStatDto(ScoreStat scoreStat) {
        BeanUtils.copyProperties(scoreStat, this);
    }

    public ScoreStatDto(ScoreStatVo scoreStatVo) {
        BeanUtils.copyProperties(scoreStatVo, this);
    }

    @JSONField(serialize = false)
    public ScoreStat getEntity() {
        return BeanUtils.copyProperties(this, ScoreStat.class);
    }

    @JSONField(serialize = false)
    public ScoreStatVo getVo() {
        return BeanUtils.copyProperties(this, ScoreStatVo.class);
    }


}
