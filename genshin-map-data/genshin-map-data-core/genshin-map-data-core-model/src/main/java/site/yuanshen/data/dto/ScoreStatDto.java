package site.yuanshen.data.dto;

import com.alibaba.fastjson2.annotation.JSONField;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.With;
import site.yuanshen.common.core.utils.BeanUtils;
import site.yuanshen.data.entity.ScoreStat;
import site.yuanshen.data.vo.ScoreStatVo;

import java.time.LocalDateTime;
import java.util.Map;


/**
 * 评分统计路数据封装
 *
 * @since 2023-04-22 06:47:07
 */
@Data
@With
@NoArgsConstructor
@AllArgsConstructor
@Schema(title = "ScoreStat数据封装", description = "评分统计数据封装")
public class ScoreStatDto {

    /**
     * 乐观锁
     */
    private Long version;

    /**
     * ID
     */
    private Long id;

    /**
     * 更新人
     */
    private Long updaterId;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;

    /**
     * 统计范围
     */
    private String scope;

    /**
     * 统计颗粒度
     */
    private String span;

    /**
     * 统计起始时间
     */
    private LocalDateTime spanStartTime;

    /**
     * 统计终止时间
     */
    private LocalDateTime spanEndTime;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 修改的字段JSON
     */
    private Map<String, Object> content;

    public ScoreStatDto(ScoreStat scoreStat) {
        BeanUtils.copy(scoreStat, this);
    }

    public ScoreStatDto(ScoreStatVo scoreStatVo) {
        BeanUtils.copy(scoreStatVo, this);
    }

    @JSONField(serialize = false)
    public ScoreStat getEntity() {
        return BeanUtils.copy(this, ScoreStat.class);
    }

    @JSONField(serialize = false)
    public ScoreStatVo getVo() {
        return BeanUtils.copy(this, ScoreStatVo.class);
    }

}