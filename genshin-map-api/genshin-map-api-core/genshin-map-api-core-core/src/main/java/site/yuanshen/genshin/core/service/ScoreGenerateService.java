package site.yuanshen.genshin.core.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import site.yuanshen.data.dto.adapter.score.ScoreSpanConfigDto;
import site.yuanshen.data.entity.History;
import site.yuanshen.data.entity.Marker;
import site.yuanshen.data.entity.ScoreStat;
import site.yuanshen.data.enums.ScoreScopeEnum;
import site.yuanshen.data.vo.adapter.score.ScoreDataPunctuateVo;
import site.yuanshen.data.vo.adapter.score.ScoreParamsVo;
import site.yuanshen.genshin.core.service.helper.score.ScoreGeneratePunctuateHelper;
import site.yuanshen.genshin.core.service.helper.score.ScoreHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 评分生成接口
 *
 * @author Alex Fang
 */
@Service
@RequiredArgsConstructor
public class ScoreGenerateService {
    private final ScoreHelper commonHelper;
    private final ScoreGeneratePunctuateHelper punctuateHelper;

    public void generateScore(ScoreParamsVo config) {
        final String scope = config.getScope();
        final ScoreSpanConfigDto span = config.calculateSpan();
        final Long generatorId = config.getGeneratorId();

        if(ScoreScopeEnum.PUNCTUATE.name().equals(scope))
            this.generateScorePunctuate(span, generatorId);
    }

    private void generateScorePunctuate(ScoreSpanConfigDto span, Long generatorId) {
        commonHelper.clearData(ScoreScopeEnum.PUNCTUATE.name(), span);

        final List<History> historyList = punctuateHelper.getHistoryList(span);
        final List<Marker> markerList = punctuateHelper.getHistoryMarkers(historyList);
        final List<Marker> markerCreateList = punctuateHelper.getMarkerCreateList(span);
        final List<Marker> markerFullList = new ArrayList<>();
        markerFullList.addAll(markerList);
        markerFullList.addAll(markerCreateList);

        // 生成首次与末次记录
        final List<History> initializeHistory = punctuateHelper.getInitializeHistory(span, markerFullList);
        final List<History> finalizeHistory = punctuateHelper.getFinalizeHistory(span, markerFullList);

        // 合并日志
        final List<History> fullHistory = new ArrayList<>();
        fullHistory.addAll(initializeHistory);
        fullHistory.addAll(finalizeHistory);
        fullHistory.addAll(historyList);

        // 日志分组
        Map<Long, List<History>> historyGroup = punctuateHelper.getHistoryGroup(fullHistory);
        // 生成模块化日志
        Map<ScoreHelper.ScoreKey, ScoreDataPunctuateVo> fieldsDiff = punctuateHelper.getHistoryFieldDiff(span, null, historyGroup);

        // 保存日志
        final List<ScoreStat> scoreData = punctuateHelper.getScoreData(generatorId, span, fieldsDiff);
        commonHelper.saveData(scoreData, true);
    }
}
