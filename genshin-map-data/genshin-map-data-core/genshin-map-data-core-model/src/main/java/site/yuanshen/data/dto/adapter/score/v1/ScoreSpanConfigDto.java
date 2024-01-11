package site.yuanshen.data.dto.adapter.score.v1;

import lombok.Data;
import lombok.experimental.Accessors;
import site.yuanshen.common.core.exception.GenshinApiException;
import site.yuanshen.common.core.utils.TimeUtils;
import site.yuanshen.common.core.utils.TimeWrapper;
import site.yuanshen.data.enums.score.v1.ScoreSpanEnum;

import java.sql.Timestamp;

@Data
@Accessors(chain = true)
public class ScoreSpanConfigDto {
    public static ScoreSpanConfigDto create() {
        return new ScoreSpanConfigDto();
    }

    /**
     * 区间
     */
    private ScoreSpanEnum span;

    /**
     * 开始时间
     */
    private Timestamp startTime;

    /**
     * 是否包含开始时间
     */
    private boolean startTimeInclude = false;

    /**
     * 结束时间
     */
    private Timestamp endTime;

    /**
     * 是否包含结束时间
     */
    private boolean endTimeInclude = false;

    /**
     * 区间开始时间
     */
    private Timestamp spanStartTime;

    /**
     * 区间结束时间
     */
    private Timestamp spanEndTime;

    /**
     * ----------------------------------------
     * 辅助方法
     * ----------------------------------------
     */
    private void validateData() {
        if(this.startTime == null || this.endTime == null) {
            throw new GenshinApiException("无效的时间范围");
        }
        if(this.span == null) {
            throw new GenshinApiException("无效的时间颗粒度");
        }
    }

    public ScoreSpanConfigDto calculateSpan() {
        this.validateData();

        TimeWrapper timeStart = TimeWrapper.create().setTime(this.startTime);
        TimeWrapper timeEnd = TimeWrapper.create().setTime(this.endTime);

        if(!this.startTimeInclude)
            timeStart.offsetSecond(TimeUtils.ONE_DAY_SECOND);
        if(!this.endTimeInclude)
            timeEnd.offsetSecond(-TimeUtils.ONE_DAY_SECOND);

        if(ScoreSpanEnum.DAY.equals(this.span)) {
            timeStart.toFirstSecond();
            timeEnd.toLastSecond();
        }

        this.spanStartTime = timeStart.getTime();
        this.spanEndTime = timeEnd.getTime();

        return this;
    }

    public boolean isTimeMatch(Timestamp ts) {
        return !this.spanStartTime.after(ts) && !this.spanEndTime.before(ts);
    }

    public static ScoreSpanConfigDto calibrateSpan(ScoreSpanEnum span, Timestamp ts) {
        return create()
                .setSpan(span)
                .setStartTime(ts)
                .setStartTimeInclude(true)
                .setEndTime(ts)
                .setEndTimeInclude(true)
                .calculateSpan();

    }
}
