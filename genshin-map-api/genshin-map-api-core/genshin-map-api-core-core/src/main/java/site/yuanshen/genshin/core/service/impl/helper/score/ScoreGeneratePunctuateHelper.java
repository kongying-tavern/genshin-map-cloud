package site.yuanshen.genshin.core.service.impl.helper.score;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Component;
import site.yuanshen.common.core.utils.BeanUtils;
import site.yuanshen.common.core.utils.DiffUtils;
import site.yuanshen.common.core.utils.JsonUtils;
import site.yuanshen.data.base.BaseEntity;
import site.yuanshen.data.dto.MarkerDto;
import site.yuanshen.data.dto.adapter.score.ScoreSpanConfigDto;
import site.yuanshen.data.entity.History;
import site.yuanshen.data.entity.Marker;
import site.yuanshen.data.entity.ScoreStat;
import site.yuanshen.data.enums.ScoreScopeEnum;
import site.yuanshen.data.mapper.HistoryMapper;
import site.yuanshen.data.mapper.MarkerMapper;
import site.yuanshen.genshin.core.convert.HistoryConvert;

import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class ScoreGeneratePunctuateHelper {
    private final HistoryMapper historyMapper;
    private final MarkerMapper markerMapper;

    private final static Function<String, String> strHandler = v -> StringUtils.defaultIfEmpty(v, "");
    private final static Function<Integer, Integer> intHandler = v -> v == null ? 0 : v;
    private final static Function<Long, Long> longHandler = v -> v == null ? 0 : v;

    @Data
    public static class R {
        private Map<String, Integer> fields = new HashMap<>();
        private Map<String, Integer> chars = new HashMap<>();
    }

    /**
     * 获取打点的历史记录
     * @param span
     * @return
     */
    public List<History> getHistoryList(ScoreSpanConfigDto span) {
        final List<History> listHistory = historyMapper.selectList(
                Wrappers.<History>lambdaQuery()
                        .eq(History::getType, 4)
                        .eq(BaseEntity::getDelFlag, 0)
                        .ge(BaseEntity::getCreateTime, span.getSpanStartTime())
                        .le(BaseEntity::getCreateTime, span.getSpanEndTime())
                );
        return listHistory;
    }

    /**
     * 获取历史记录对应的点位数据
     * @param historyList
     * @return
     */
    public List<Marker> getHistoryMarkers(List<History> historyList) {
        final List<Long> markerIds = historyList.stream().map(History::getTId).collect(Collectors.toList());
        List<Marker> markerList = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(markerIds)) {
            markerList = markerMapper.selectList(
                    Wrappers.<Marker>lambdaQuery()
                            .in(Marker::getId, markerIds)
            );
        }
        return markerList;
    }

    /**
     * 生成点位的首次记录
     * @param markerList
     * @return
     */
    public List<History> getInitializeHistory(List<Marker> markerList) {
        final List<History> initializeHistory = markerList
                .stream()
                .map(marker -> {
                    MarkerDto o = new MarkerDto();
                    o.setId(marker.getId());
                    History history = HistoryConvert.convert(o);
                    history.setCreatorId(marker.getCreatorId());
                    history.setCreateTime(marker.getCreateTime());
                    history.setUpdaterId(marker.getCreatorId());
                    history.setUpdateTime(marker.getCreateTime());
                    return history;
                })
                .collect(Collectors.toList());
        return initializeHistory;
    }

    /**
     * 生成点位的末次记录
     * @param markerList
     * @return
     */
    public List<History> getFinalizeHistory(List<Marker> markerList) {
        final List<History> finalizeHistory = markerList
                .stream()
                .map(marker -> {
                    MarkerDto o = new MarkerDto();
                    BeanUtils.copyNotNull(marker, o);
                    History history = HistoryConvert.convert(o);
                    history.setUpdateTime(LocalDateTime.MAX);
                    history.setCreateTime(LocalDateTime.MAX);
                    return history;
                })
                .collect(Collectors.toList());
        return finalizeHistory;
    }

    /**
     * 获取记录分组
     * @param historyList
     * @return
     */
    public Map<Long, List<History>> getHistoryGroup(List<History> historyList) {
        final Map<Long, List<History>> historyGroup = historyList
                .stream()
                .sorted(Comparator.nullsFirst(Comparator.comparing(History::getCreateTime)))
                .collect(Collectors.groupingBy(History::getTId));
        return historyGroup;
    }

    /**
     * 获取比对设置
     * @return
     */
    public DiffUtils.FieldDiffConfig getDiffConfig() {
        final DiffUtils.FieldDiffConfig config = DiffUtils.FieldDiffConfig.create()
                .setIgnore(Arrays.asList(
                        "log",
                        "version",
                        "id",
                        "markerCreatorId",
                        "pictureCreatorId",
                        "itemList"
                ))
                .setActionsPre("hiddenFlag", intHandler)
                .setActionsPre("refreshTime", longHandler)
                .setActionsPre("position", strHandler)
                .setActionsPre("markerTitle", strHandler)
                .setActionsPre("content", strHandler)
                .setActionsPre("picture", strHandler)
                .setActionsPre("videoPath", strHandler)
                .setIgnoreBeforeNull(false)
                .setIgnoreAfterNull(false);
        return config;
    }

    /**
     * 生成字段差异
     * @param span
     * @param historyGroup
     * @return
     */
    public Map<ScoreGenerateHelper.ScoreKey, R> getHistoryFieldDiff(
            ScoreSpanConfigDto span,
            Map<ScoreGenerateHelper.ScoreKey, R> diff,
            Map<Long, List<History>> historyGroup
    ) {
        if(diff == null) {
            diff = new HashMap<>();
        }

        for(Map.Entry<Long, List<History>> group : historyGroup.entrySet()) {
            List<History> histories = group.getValue();
            if(CollectionUtils.isNotEmpty(histories)) {
                int historySize = histories.size();
                for(int i = 1; i < historySize; i++) {
                    final History historyBefore = histories.get(i - 1);
                    final History historyAfter = histories.get(i);
                    final MarkerDto historyBeforeData = JsonUtils.jsonToObject(historyBefore.getContent(), MarkerDto.class);
                    final MarkerDto historyAfterData = JsonUtils.jsonToObject(historyAfter.getContent(), MarkerDto.class);

                    ScoreGenerateHelper.ScoreKey mapKey = ScoreGenerateHelper.getScoreKey(span, historyBefore.getCreatorId(), historyBefore.getCreateTime());
                    if(!diff.containsKey(mapKey)) {
                        diff.put(mapKey, new R());
                    }

                    // 1) 生成字段差异数据
                    List<DiffUtils.FieldDiff> historyDiffs = DiffUtils.getFieldsDiff(historyBeforeData, historyAfterData, this.getDiffConfig());
                    for(DiffUtils.FieldDiff historyDiff : historyDiffs) {
                        final String diffKey = historyDiff.getKey();
                        diff.get(mapKey).getFields().compute(diffKey, (k, v) -> v == null ? 1 : v + 1);
                    }

                    // 生成文本差异数据
                    diff.get(mapKey).getChars().compute("markerTitle", (k, v) -> (v == null ? 0 : v) + (new DiffUtils.Levenshtein(historyBeforeData.getMarkerTitle(), historyAfterData.getMarkerTitle())).calculateDistance().getDistance());
                    diff.get(mapKey).getChars().compute("content", (k, v) -> (v == null ? 0 : v) + (new DiffUtils.Levenshtein(historyBeforeData.getContent(), historyAfterData.getContent())).calculateDistance().getDistance());
                }
            }
        }

        return diff;
    }

    public List<ScoreStat> getScoreData(
            Long operatorId,
            ScoreSpanConfigDto span,
            Map<ScoreGenerateHelper.ScoreKey, R> stat
    ) {
        final String spanName = span.getSpan().name();
        final List<ScoreStat> scoreList = new ArrayList<>();
        stat.forEach((scoreKey, scoreVal) -> {
            if(scoreVal != null) {
                final ScoreStat scoreStat = (new ScoreStat())
                        .setScope(ScoreScopeEnum.PUNCTUATE.name())
                        .setSpan(spanName)
                        .setUserId(scoreKey.getUserId())
                        .setSpanStartTime(scoreKey.getSpanStartTime())
                        .setSpanEndTime(scoreKey.getSpanEndTime())
                        .setContent(JSON.toJSONString(scoreVal));
                scoreStat.setCreatorId(operatorId);
                scoreList.add(scoreStat);
            }
        });

        return scoreList;
    }
}
