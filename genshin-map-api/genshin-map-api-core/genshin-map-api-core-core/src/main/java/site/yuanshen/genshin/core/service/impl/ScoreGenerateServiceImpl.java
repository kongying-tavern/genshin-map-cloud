package site.yuanshen.genshin.core.service.impl;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import kotlin.Triple;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Service;
import site.yuanshen.common.core.utils.BeanUtils;
import site.yuanshen.common.core.utils.DiffUtils;
import site.yuanshen.common.core.utils.JsonUtils;
import site.yuanshen.common.core.utils.TimeUtils;
import site.yuanshen.data.base.BaseEntity;
import site.yuanshen.data.dto.MarkerDto;
import site.yuanshen.data.dto.ScoreStatDto;
import site.yuanshen.data.dto.adapter.score.ScoreSpanConfigDto;
import site.yuanshen.data.entity.History;
import site.yuanshen.data.entity.Marker;
import site.yuanshen.data.entity.ScoreStat;
import site.yuanshen.data.enums.ScoreScopeEnum;
import site.yuanshen.data.mapper.HistoryMapper;
import site.yuanshen.data.mapper.MarkerMapper;
import site.yuanshen.data.mapper.ScoreStatMapper;
import site.yuanshen.data.vo.adapter.score.ScoreGenerateVo;
import site.yuanshen.genshin.core.convert.HistoryConvert;
import site.yuanshen.genshin.core.service.ScoreGenerateService;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 评分生成接口
 *
 * @author Alex Fang
 */
@Service
@RequiredArgsConstructor
public class ScoreGenerateServiceImpl implements ScoreGenerateService {
    private final HistoryMapper historyMapper;
    private final MarkerMapper markerMapper;
    private final ScoreStatMapper scoreStatMapper;

    private final static String tz = "Asia/Shanghai";

    @Override
    public void generateScore(ScoreGenerateVo config) {
        final String scope = config.getScope();
        final ScoreSpanConfigDto span = config.calculateSpan();
        final Long generatorId = config.getGeneratorId();

        if(ScoreScopeEnum.PUNCTUATE.name().equals(scope))
            this.generateScorePunctuate(span, generatorId);
    }

    private void generateScorePunctuate(ScoreSpanConfigDto span, Long generatorId) {
        final List<History> listRecords = new ArrayList<>();
        final List<History> listHistory = historyMapper.selectList(
                Wrappers.<History>lambdaQuery()
                        .eq(History::getType, 4)
                        .eq(BaseEntity::getDelFlag, 0)
                        .ge(BaseEntity::getCreateTime, span.getSpanStartTime())
                        .le(BaseEntity::getCreateTime, span.getSpanEndTime())
                );
        final List<Long> markerIds = listHistory.stream().map(History::getTId).collect(Collectors.toList());
        List<Marker> listMarkers = new ArrayList<>();
        if(CollectionUtils.isNotEmpty(markerIds)) {
            listMarkers = markerMapper.selectList(
                    Wrappers.<Marker>lambdaQuery()
                            .in(Marker::getId, markerIds)
            );
        }

        // 生成首次与末次记录
        final List<History> listHistoryInitialize = listMarkers
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
        final List<History> listHistoryFinalize = listMarkers
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

        // 合并日志
        listRecords.addAll(listHistoryInitialize);
        listRecords.addAll(listHistoryFinalize);
        listRecords.addAll(listHistory);

        final Map<Long, List<History>> groupRecords = listRecords
                .stream()
                .sorted(Comparator.nullsFirst(Comparator.comparing(History::getCreateTime)))
                .collect(Collectors.groupingBy(History::getTId));

        // 生成日志
        final Map<Triple<Long, LocalDateTime, LocalDateTime>, HashMap<String, Integer>> stats = new HashMap<>();
        Function<String, String> strHandler = v -> StringUtils.defaultIfEmpty(v, "");
        Function<Integer, Integer> intHandler = v -> v == null ? 0 : v;
        Function<Long, Long> longHandler = v -> v == null ? 0 : v;
        for(Map.Entry<Long, List<History>> group : groupRecords.entrySet()) {
            List<History> histories = group.getValue();
            if(CollectionUtils.isNotEmpty(histories)) {
                int historySize = histories.size();
                for(int i = 1; i < historySize; i++) {
                    final History historyBefore = histories.get(i - 1);
                    final History historyAfter = histories.get(i);
                    final Long operatorId = historyBefore.getCreatorId();
                    final LocalDateTime operateTime = historyBefore.getCreateTime();
                    final MarkerDto historyBeforeData = JsonUtils.jsonToObject(historyBefore.getContent(), MarkerDto.class);
                    final MarkerDto historyAfterData = JsonUtils.jsonToObject(historyAfter.getContent(), MarkerDto.class);
                    List<DiffUtils.FieldDiff> historyDiff = DiffUtils.getFieldsDiff(historyBeforeData, historyAfterData, DiffUtils.FieldDiffConfig.create()
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
                            .setIgnoreAfterNull(false)
                    );

                    // 构造数据
                    final ScoreSpanConfigDto spanPeriod = ScoreSpanConfigDto.calibrateSpan(span.getSpan(), TimeUtils.toTimestamp(operateTime, tz));
                    final LocalDateTime spanStart = LocalDateTime.ofInstant(spanPeriod.getSpanStartTime().toInstant(), ZoneId.of(tz));
                    final LocalDateTime spanEnd = LocalDateTime.ofInstant(spanPeriod.getSpanEndTime().toInstant(), ZoneId.of(tz));
                    final Triple<Long, LocalDateTime, LocalDateTime> mapKey = new Triple(operatorId, spanStart, spanEnd);
                    if(!stats.containsKey(mapKey)) {
                        stats.put(mapKey, new HashMap<>());
                    }
                    for(DiffUtils.FieldDiff diff : historyDiff) {
                        final String diffKey = diff.getKey();
                        final int diffCount = stats.get(mapKey).getOrDefault(diffKey, 0);
                        stats.get(mapKey).put(diffKey, diffCount + 1);
                    }
                }
            }
        }

        // 生成日志
        ScoreStatDto statsDefault = new ScoreStatDto();
        statsDefault.setScope(ScoreScopeEnum.PUNCTUATE.name());
        statsDefault.setSpan(span.getSpan().name());
        final List<ScoreStat> statList = new ArrayList<>();
        for(Map.Entry<Triple<Long, LocalDateTime, LocalDateTime>, HashMap<String, Integer>> stat : stats.entrySet()) {
            final ScoreStat statData = new ScoreStat();
            final Triple<Long, LocalDateTime, LocalDateTime> diffKey = stat.getKey();
            final Map<String, Integer> diffVal = stat.getValue();
            if(diffVal.size() > 0) {
                BeanUtils.copyNotNull(statsDefault, statData);
                statData.setCreatorId(generatorId);
                statData.setUserId(diffKey.getFirst());
                statData.setSpanStartTime(diffKey.getSecond());
                statData.setSpanEndTime(diffKey.getThird());
                statData.setContent(JSON.toJSONString(diffVal));
                statList.add(statData);
            }
        }

        statList.parallelStream().forEach(i -> {
            scoreStatMapper.insert(i);
        });
    }
}
