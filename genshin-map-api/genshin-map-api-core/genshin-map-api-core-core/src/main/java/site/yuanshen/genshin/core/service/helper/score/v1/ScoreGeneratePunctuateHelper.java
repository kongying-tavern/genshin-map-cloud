package site.yuanshen.genshin.core.service.helper.score.v1;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import site.yuanshen.common.core.utils.BeanUtils;
import site.yuanshen.common.core.utils.DiffUtils;
import site.yuanshen.common.core.utils.JsonUtils;
import site.yuanshen.common.core.utils.TimeUtils;
import site.yuanshen.data.base.BaseEntity;
import site.yuanshen.data.dto.MarkerDto;
import site.yuanshen.data.dto.adapter.score.v1.ScoreSpanConfigDto;
import site.yuanshen.data.entity.*;
import site.yuanshen.data.enums.HistoryEditType;
import site.yuanshen.data.enums.score.v1.ScoreScopeEnum;
import site.yuanshen.data.mapper.HistoryMapper;
import site.yuanshen.data.mapper.ItemMapper;
import site.yuanshen.data.mapper.MarkerItemLinkMapper;
import site.yuanshen.data.mapper.MarkerMapper;
import site.yuanshen.data.vo.adapter.score.v1.ScoreDataPunctuateVo;
import site.yuanshen.genshin.core.convert.HistoryConvert;

import java.sql.Timestamp;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class ScoreGeneratePunctuateHelper {
    private final HistoryMapper historyMapper;
    private final MarkerMapper markerMapper;
    private final MarkerItemLinkMapper markerItemLinkMapper;
    private final ItemMapper itemMapper;

    private final static Function<String, String> strHandler = v -> StrUtil.emptyToDefault(v, "");
    private final static Function<Integer, Integer> intHandler = v -> v == null ? 0 : v;
    private final static Function<Long, Long> longHandler = v -> v == null ? 0 : v;

    /**
     * 获取时间范围内打点的历史记录
     * @param span
     * @return
     */
    public List<History> getHistoryList(ScoreSpanConfigDto span) {
        final List<History> historyList = historyMapper.selectList(
                Wrappers.<History>lambdaQuery()
                        .eq(History::getType, 4)
                        .eq(BaseEntity::getDelFlag, false)
                        .ge(BaseEntity::getCreateTime, span.getSpanStartTime())
                        .le(BaseEntity::getCreateTime, span.getSpanEndTime())
                );
        return historyList;
    }

    /**
     * 获取时间范围内创建的点位
     * @param span
     * @return
     */
    public List<Marker> getMarkerCreateList(ScoreSpanConfigDto span) {
        final List<Marker> markerList = markerMapper.selectList(
                Wrappers.<Marker>lambdaQuery()
                        .ge(BaseEntity::getCreateTime, span.getSpanStartTime())
                        .le(BaseEntity::getCreateTime, span.getSpanEndTime())
        );
        return markerList;
    }

    /**
     * 获取历史记录对应的点位数据
     * @param historyList
     * @return
     */
    public List<Marker> getHistoryMarkers(List<History> historyList) {
        final List<Long> markerIds = historyList.stream().map(History::getTId).collect(Collectors.toList());
        List<Marker> markerList = new ArrayList<>();
        if (CollectionUtil.isNotEmpty(markerIds)) {
            markerList = markerMapper.selectList(
                    Wrappers.<Marker>lambdaQuery()
                            .in(Marker::getId, markerIds)
            );
        }
        return markerList;
    }

    /**
     * 获取点位初始化的物品
     * @param span
     * @param markerList
     * @return
     */
    public Map<Long, Item> getInitializeMarkerItemMap(ScoreSpanConfigDto span, List<Marker> markerList) {
        if(CollectionUtil.isEmpty(markerList)) {
            return new HashMap<>();
        }

        final List<Long> markerIds = markerList.stream()
                .map(Marker::getId)
                .collect(Collectors.toList());
        if(CollectionUtil.isEmpty(markerIds)) {
            return new HashMap<>();
        }
        final List<MarkerItemLink> markerItemLinkList = markerItemLinkMapper.selectList(
                Wrappers.<MarkerItemLink>lambdaQuery()
                        .in(MarkerItemLink::getMarkerId, markerIds)
                        .orderByAsc(MarkerItemLink::getCreateTime)
                        .orderByAsc(MarkerItemLink::getId)
        );
        final Map<Long, Long> markerItemLinkMap = markerItemLinkList.stream()
                .collect(Collectors.toMap(
                        MarkerItemLink::getMarkerId,
                        MarkerItemLink::getItemId,
                        (o, n) -> o
                ));
        final List<Long> itemIds = markerItemLinkMap
                .values()
                .stream().collect(Collectors.toList());
        if(CollectionUtil.isEmpty(itemIds)) {
            return new HashMap<>();
        }
        final List<Item> itemList = itemMapper.selectList(
                Wrappers.<Item>lambdaQuery()
                        .in(Item::getId, itemIds)
        );
        final Map<Long, Item> itemMap = itemList.stream()
                .collect(Collectors.toMap(
                        Item::getId,
                        v -> v,
                        (o, n) -> n
                ));
        Map<Long, Item> markerItemMap = markerIds.stream()
                .collect(Collectors.toMap(
                        markerId -> markerId,
                        markerId -> {
                            final Long itemId = markerItemLinkMap.getOrDefault(markerId, 0L);
                            final Item item = itemMap.getOrDefault(itemId, new Item());
                            return item;
                        },
                        (o, n) -> n
                ));
        return markerItemMap;
    }

    /**
     * 获取点位在区间前的最后一次数据映射关系
     * @param span
     * @param markerList
     * @return
     */
    public Map<Long, History> getInitializeHistoryMap(ScoreSpanConfigDto span, List<Marker> markerList) {
        if(CollectionUtil.isEmpty(markerList)) {
            return new HashMap<>();
        }

        final List<Long> markerIds = markerList.stream().map(Marker::getId).collect(Collectors.toList());
        if(CollectionUtil.isEmpty(markerIds)) {
            return new HashMap<>();
        }

        final List<History> historyList = historyMapper.selectList(
                Wrappers.<History>lambdaQuery()
                        .in(History::getTId, markerIds)
                        .eq(History::getType, 4)
                        .eq(BaseEntity::getDelFlag, false)
                        .lt(BaseEntity::getCreateTime, span.getSpanStartTime())
                        .orderByDesc(History::getCreateTime)
        );
        final Map<Long, History> historyMap = historyList.stream()
                .collect(Collectors.toMap(
                        History::getTId,
                        v -> v,
                        (o, n) -> o
                ));
        return historyMap;
    }

    /**
     * 生成点位分组首次比对条目
     * 注：此过程分为两种情况
     * 1. 若点位创建时间在区间内，则使用点位关联的分类添加初始比对记录
     * 2. 若点位创建时间不在区间内，则不添加初始比对记录
     * @param span
     * @param markerList
     * @return
     */
    public List<History> getInitializeHistory(ScoreSpanConfigDto span, List<Marker> markerList) {
        final Map<Long, Item> markerItemMap = this.getInitializeMarkerItemMap(span, markerList);

        final List<History> initializeHistory = markerList
                .stream()
                .map(marker -> {
                    Timestamp createTime = marker.getCreateTime();
                    if(span.isTimeMatch(createTime)) {
                        final Long markerId = marker.getId();
                        final Item markerItem = markerItemMap.getOrDefault(markerId, new Item());
                        final String markerItemTitle = StrUtil.emptyToDefault(markerItem.getName(), "");
                        final String markerItemContent = StrUtil.emptyToDefault(markerItem.getDefaultContent(), "");
                        // 生成点位DTO
                        MarkerDto o = new MarkerDto();
                        o.setId(markerId);
                        o.setMarkerTitle(markerItemTitle);
                        o.setContent(markerItemContent);
                        // 生成历史数据
                        final History history = HistoryConvert.convert(o, HistoryEditType.NONE);
                        history.setCreatorId(marker.getCreatorId());
                        history.setCreateTime(marker.getCreateTime());
                        history.setUpdaterId(marker.getCreatorId());
                        history.setUpdateTime(marker.getCreateTime());
                        return history;
                    }
                    return null;
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        return initializeHistory;
    }


    /**
     * 获取点位在区间后的第一次数据映射关系
     * @param span
     * @param markerList
     * @return
     */
    public Map<Long, History> getFinalizeHistoryMap(ScoreSpanConfigDto span, List<Marker> markerList) {
        if(CollectionUtil.isEmpty(markerList)) {
            return new HashMap<>();
        }

        final List<Long> markerIds = markerList.stream().map(Marker::getId).collect(Collectors.toList());
        if(CollectionUtil.isEmpty(markerIds)) {
            return new HashMap<>();
        }

        final List<History> historyList = historyMapper.selectList(
                Wrappers.<History>lambdaQuery()
                        .in(History::getTId, markerIds)
                        .eq(History::getType, 4)
                        .eq(BaseEntity::getDelFlag, false)
                        .gt(BaseEntity::getCreateTime, span.getSpanEndTime())
                        .orderByAsc(History::getCreateTime)
        );
        final Map<Long, History> historyMap = historyList.stream()
                .collect(Collectors.toMap(
                        History::getTId,
                        v -> v,
                        (o, n) -> o
                ));
        return historyMap;
    }

    /**
     * 生成点位分组末次比对条目
     * 注：此过程分为两种情况
     * 1. 若此区间后有记录，表示最后一次修改时，将点位数据改为区间后一条记录的值，则使用区间后一条数据进行末次比对
     * 2. 若此区间后无记录，表示最后一次修改时，改为现有点位数据，则使用现有点位信息创建历史记录进行末次比对
     * @param span
     * @param markerList
     * @return
     */
    public List<History> getFinalizeHistory(ScoreSpanConfigDto span, List<Marker> markerList) {
        final Map<Long, History> historyMap = this.getFinalizeHistoryMap(span, markerList);

        final List<History> finalizeHistory = markerList
                .stream()
                .map(marker -> {
                    final Long markerId = marker.getId();
                    History history = historyMap.get(markerId);

                    if(history == null) {
                        MarkerDto o = BeanUtils.copy(marker, MarkerDto.class);
                        history = HistoryConvert.convert(o, HistoryEditType.NONE);
                        history.setCreatorId(marker.getUpdaterId());
                        history.setCreateTime(TimeUtils.toTimeOffsetInSecond(marker.getUpdateTime(), 100L));
                        history.setUpdaterId(marker.getUpdaterId());
                        history.setUpdateTime(TimeUtils.toTimeOffsetInSecond(marker.getUpdateTime(), 100L));
                    }
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
    public Map<ScoreHelper.ScoreKey, ScoreDataPunctuateVo> getHistoryFieldDiff(
            ScoreSpanConfigDto span,
            Map<ScoreHelper.ScoreKey, ScoreDataPunctuateVo> diff,
            Map<Long, List<History>> historyGroup
    ) {
        if(diff == null) {
            diff = new HashMap<>();
        }

        for(Map.Entry<Long, List<History>> group : historyGroup.entrySet()) {
            List<History> histories = group.getValue();
            if(CollectionUtil.isNotEmpty(histories)) {
                int historySize = histories.size();
                for(int i = 1; i < historySize; i++) {
                    final History historyBefore = histories.get(i - 1);
                    final History historyAfter = histories.get(i);
                    final MarkerDto historyBeforeData = JsonUtils.jsonToObject(historyBefore.getContent(), MarkerDto.class);
                    final MarkerDto historyAfterData = JsonUtils.jsonToObject(historyAfter.getContent(), MarkerDto.class);

                    // todo
                    final ScoreHelper.ScoreKey mapKey = ScoreHelper.getScoreKey(span, historyBefore.getCreatorId(), historyBefore.getCreateTime());
                    if(!diff.containsKey(mapKey)) {
                        diff.put(mapKey, new ScoreDataPunctuateVo());
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
            Map<ScoreHelper.ScoreKey, ScoreDataPunctuateVo> stat
    ) {
        final String spanName = span.getSpan().name();
        final List<ScoreStat> scoreList = new ArrayList<>();
        stat.forEach((scoreKey, scoreVal) -> {
            if(scoreVal != null) {
                final ScoreStat scoreStat = (new ScoreStat())
                        .withScope(ScoreScopeEnum.PUNCTUATE.name())
                        .withSpan(spanName)
                        .withUserId(scoreKey.getUserId())
                        .withSpanStartTime(scoreKey.getSpanStartTime())
                        .withSpanEndTime(scoreKey.getSpanEndTime())
                        .withContent(JSONObject.from(scoreVal).toJavaObject(Map.class));
                scoreStat.setCreatorId(operatorId);
                scoreList.add(scoreStat);
            }
        });

        return scoreList;
    }
}
