package site.yuanshen.genshin.core.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import site.yuanshen.common.core.utils.JsonUtils;
import site.yuanshen.data.dto.adapter.score.ScoreSpanConfigDto;
import site.yuanshen.data.entity.ScoreStat;
import site.yuanshen.data.enums.ScoreScopeEnum;
import site.yuanshen.data.vo.adapter.score.ScoreDataPackVo;
import site.yuanshen.data.vo.adapter.score.ScoreDataPunctuateVo;
import site.yuanshen.data.vo.adapter.score.ScoreParamsVo;
import site.yuanshen.genshin.core.service.ScoreDataService;
import site.yuanshen.genshin.core.service.impl.helper.score.ScoreHelper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ScoreDataServiceImpl implements ScoreDataService {
    private final ScoreHelper commonHelper;

    @Override
    public List<? extends ScoreDataPackVo> getData(ScoreParamsVo config) {
        final String scope = config.getScope();
        final ScoreSpanConfigDto span = config.calculateSpan();

        if(ScoreScopeEnum.PUNCTUATE.name().equals(scope))
            return this.getDataPunctuate(span);

        return new ArrayList<>();
    }

    private List<ScoreDataPackVo<ScoreDataPunctuateVo>> getDataPunctuate(ScoreSpanConfigDto span) {
        final String scope = ScoreScopeEnum.PUNCTUATE.name();
        final List<ScoreStat> score = commonHelper.getData(scope, span);
        final Map<Long, ScoreDataPackVo<ScoreDataPunctuateVo>> scoreDataMap = new HashMap<>();
        score.forEach(scoreRow -> {
            final Long userId = scoreRow.getUserId();
            scoreDataMap.putIfAbsent(userId, (new ScoreDataPackVo())
                    .setScope(scope)
                    .setSpan(span.getSpan().name())
                    .setUserId(userId)
                    .setData(new ScoreDataPunctuateVo())
            );
            final ScoreDataPunctuateVo scoreContent = JsonUtils.jsonToObject(scoreRow.getContent(), ScoreDataPunctuateVo.class);
            scoreDataMap.get(userId).getData().merge(scoreContent);
        });

        return scoreDataMap.values().stream().collect(Collectors.toList());
    }
}
