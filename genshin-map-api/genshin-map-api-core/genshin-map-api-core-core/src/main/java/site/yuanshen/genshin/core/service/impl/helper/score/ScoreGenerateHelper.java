package site.yuanshen.genshin.core.service.impl.helper.score;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import org.springframework.stereotype.Component;
import site.yuanshen.common.core.utils.TimeUtils;
import site.yuanshen.data.dto.adapter.score.ScoreSpanConfigDto;
import site.yuanshen.data.entity.ScoreStat;
import site.yuanshen.data.mapper.ScoreStatMapper;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.stream.Stream;

@Component
@RequiredArgsConstructor
public class ScoreGenerateHelper {
    private final ScoreStatMapper scoreStatMapper;
    private final static String tz = "Asia/Shanghai";

    @Data
    @Accessors(chain = true)
    public static class ScoreKey {
        private Long userId;

        private LocalDateTime spanStartTime;

        private LocalDateTime spanEndTime;
    }

    public static ScoreKey getScoreKey(ScoreSpanConfigDto span, Long operatorId, Timestamp ts) {
        final ScoreSpanConfigDto spanPeriod = ScoreSpanConfigDto.calibrateSpan(span.getSpan(), ts);
        final LocalDateTime spanStart = LocalDateTime.ofInstant(spanPeriod.getSpanStartTime().toInstant(), ZoneId.of(tz));
        final LocalDateTime spanEnd = LocalDateTime.ofInstant(spanPeriod.getSpanEndTime().toInstant(), ZoneId.of(tz));
        final ScoreKey scoreKey = (new ScoreKey())
                .setUserId(operatorId)
                .setSpanStartTime(spanStart)
                .setSpanEndTime(spanEnd);
        return scoreKey;
    }

    public static ScoreKey getScoreKey(ScoreSpanConfigDto span, Long operatorId, LocalDateTime dt) {
        final Timestamp ts = TimeUtils.toTimestamp(dt, tz);
        return getScoreKey(span, operatorId, ts);
    }

    public void saveData(List<ScoreStat> dataList, boolean parallel) {
        Stream<ScoreStat> stream;
        if(parallel)
            stream = dataList.parallelStream();
        else
            stream = dataList.stream();

        stream.forEach(i -> {
            scoreStatMapper.insert(i);
        });
    }
}