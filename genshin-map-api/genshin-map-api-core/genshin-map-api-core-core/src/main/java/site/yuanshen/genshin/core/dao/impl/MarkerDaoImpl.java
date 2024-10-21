package site.yuanshen.genshin.core.dao.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.date.TimeInterval;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson2.JSON;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;
import site.yuanshen.common.core.exception.GenshinApiException;
import site.yuanshen.common.core.utils.CompressUtils;
import site.yuanshen.common.core.utils.PgsqlUtils;
import site.yuanshen.data.dto.MarkerDto;
import site.yuanshen.data.dto.MarkerItemLinkDto;
import site.yuanshen.data.dto.helper.PageSearchDto;
import site.yuanshen.data.entity.Area;
import site.yuanshen.data.entity.Item;
import site.yuanshen.data.entity.Marker;
import site.yuanshen.data.entity.MarkerLinkage;
import site.yuanshen.data.enums.HiddenFlagEnum;
import site.yuanshen.data.enums.cache.CacheSplitterEnum;
import site.yuanshen.data.mapper.*;
import site.yuanshen.data.vo.MarkerItemLinkVo;
import site.yuanshen.data.vo.MarkerVo;
import site.yuanshen.data.vo.adapter.cache.MarkerCacheKeyConst;
import site.yuanshen.data.vo.adapter.cache.MarkerListCacheKey;
import site.yuanshen.data.vo.helper.PageListVo;
import site.yuanshen.genshin.core.dao.MarkerDao;

import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 点位信息的数据查询层实现
 *
 * @author Moment
 */
@Slf4j
@Service
public class MarkerDaoImpl implements MarkerDao {

    private final MarkerMapper markerMapper;
    private final MarkerItemLinkMapper markerItemLinkMapper;
    private final MarkerLinkageMapper markerLinkageMapper;
    private final ItemMapper itemMapper;
    private final AreaMapper areaMapper;
    private final CacheManager neverRefreshCacheManager;

    @Autowired
    public MarkerDaoImpl(MarkerMapper markerMapper,
                         MarkerItemLinkMapper markerItemLinkMapper,
                         MarkerLinkageMapper markerLinkageMapper,
                         ItemMapper itemMapper,
                         AreaMapper areaMapper,
                         @Qualifier("neverRefreshCacheManager")
                         CacheManager neverRefreshCacheManager) {
        this.markerMapper = markerMapper;
        this.markerItemLinkMapper = markerItemLinkMapper;
        this.markerLinkageMapper = markerLinkageMapper;
        this.itemMapper = itemMapper;
        this.areaMapper = areaMapper;
        this.neverRefreshCacheManager = neverRefreshCacheManager;
    }

    /**
     * 分页查询所有点位信息
     *
     * @param pageSearchDto 分页查询数据封装
     * @param hiddenFlagList    hidden_flag范围
     * @return 点位完整信息的前端封装的分页记录
     */
    @Override
    @Cacheable(value = "listMarkerPage")
    public PageListVo<MarkerVo> listMarkerPage(PageSearchDto pageSearchDto, List<Integer> hiddenFlagList) {
        IPage<Marker> markerPage = markerMapper.selectPageFilterByHiddenFlag(pageSearchDto.getPageEntity(),hiddenFlagList, Wrappers.lambdaQuery());
        List<Long> markerIdList = markerPage.getRecords().stream()
                .map(Marker::getId).collect(Collectors.toList());

        Map<Long, Item> itemMap = new HashMap<>();
        ConcurrentHashMap<Long, List<MarkerItemLinkVo>> markerItemLinkMap = new ConcurrentHashMap<>();
        generateMarkerItemInfo(markerIdList, itemMap, markerItemLinkMap);

        ConcurrentHashMap<Long, String> markerLinkageMap = new ConcurrentHashMap<>();
        generateMarkerLinkageInfo(markerIdList, markerLinkageMap);

        return new PageListVo<MarkerVo>()
                .setRecord(markerPage.getRecords().parallelStream()
                        .map(marker -> new MarkerDto(marker)
                            .withItemList(markerItemLinkMap.getOrDefault(marker.getId(), List.of()))
                            .withLinkageId(markerLinkageMap.getOrDefault(marker.getId(), ""))
                            .getVo())
                        .collect(Collectors.toList()))
                .setTotal(markerPage.getTotal())
                .setSize(markerPage.getSize());
    }

    /**
     * 通过ID列表查询点位信息
     *
     * @param markerIdList   点位ID列表
     * @param hiddenFlagList hidden_flag范围
     * @return 点位完整信息的数据封装列表
     */
    @Override
    @Cacheable(value = "listMarkerById")
    public List<MarkerVo> listMarkerById(List<Long> markerIdList, List<Integer> hiddenFlagList) {
        List<Marker> markerList = markerMapper.selectListWithLargeInFilterByHiddenFlag(PgsqlUtils.unnestLongStr(markerIdList),hiddenFlagList, Wrappers.lambdaQuery());

        markerIdList = markerList.stream().map(Marker::getId).collect(Collectors.toList());

        Map<Long, Item> itemMap = new HashMap<>();
        ConcurrentHashMap<Long, List<MarkerItemLinkVo>> markerItemLinkMap = new ConcurrentHashMap<>();
        generateMarkerItemInfo(markerIdList, itemMap, markerItemLinkMap);

        ConcurrentHashMap<Long, String> markerLinkageMap = new ConcurrentHashMap<>();
        generateMarkerLinkageInfo(markerIdList, markerLinkageMap);

        return markerList.parallelStream()
                        .map(marker -> new MarkerDto(marker)
                            .withItemList(markerItemLinkMap.getOrDefault(marker.getId(), List.of()))
                            .withLinkageId(markerLinkageMap.getOrDefault(marker.getId(), ""))
                            .getVo())
                        .collect(Collectors.toList());
    }

    /**
     * 生成点位物品信息 (物品 & 物品关联)
     * 物品链接 Map 为 ConcurrentHashMap 是因为对于同一个点位ID需要合并物品关联列表，在大批量处理时可能存在并发问题。
     *
     * @param markerIdList 点位ID列表
     * @param markerItemLinkMap 物品链接Map  key:marker_id, value:marker_item_link[]
     * @param itemMap 物品Map  key:item_id, value:item
     */
    public void generateMarkerItemInfo(
        List<Long> markerIdList,
        Map<Long, Item> itemMap,
        ConcurrentHashMap<Long, List<MarkerItemLinkVo>> markerItemLinkMap
    ) {
        // 获取物品ID
        final List<Long> itemIdList = markerItemLinkMapper.selectWithLargeCustomIn("marker_id", PgsqlUtils.unnestLongStr(markerIdList), Wrappers.lambdaQuery())
                .parallelStream().map(markerItemLink -> {
                    markerItemLinkMap.compute(markerItemLink.getMarkerId(),
                            (markerId, linkList) -> {
                                MarkerItemLinkVo vo = new MarkerItemLinkDto(markerItemLink).getVo();
                                if (linkList == null) return new ArrayList<>(Collections.singletonList(vo));
                                linkList.add(vo);
                                return linkList;
                            });
                    return markerItemLink.getItemId();
                })
                .distinct().collect(Collectors.toList());
        // 添加 item_id → item 映射项
        itemMap.putAll(
                itemMapper.selectListWithLargeIn(PgsqlUtils.unnestLongStr(itemIdList), Wrappers.lambdaQuery())
                    .stream()
                    .collect(Collectors.toMap(Item::getId, Item -> Item, (o, n) -> n))
        );
        // 汇总 marker_id → item_link 映射项
        markerItemLinkMap.forEach((markerId, linkVoList) ->
            linkVoList.forEach(link -> {
                final String iconTag = StrUtil.blankToDefault(itemMap.getOrDefault(link.getItemId(), new Item()).getIconTag(), "");
                link.setIconTag(iconTag);
            })
        );
    }

    /**
     * 生成点位关联信息
     *
     * @param markerIdList 点位ID列表
     * @param markerLinkageMap 点位关联Map  key:marker_id, value:linkage_id
     */
    public void generateMarkerLinkageInfo(
        List<Long> markerIdList,
        ConcurrentHashMap<Long, String> markerLinkageMap
    ) {
        if(CollUtil.isEmpty(markerIdList)) {
            return;
        }
        final List<MarkerLinkage> markerLinkageList = markerLinkageMapper.selectWithLargeMarkerIdIn(PgsqlUtils.unnestLongStr(markerIdList), Wrappers.<MarkerLinkage>lambdaQuery().eq(MarkerLinkage::getDelFlag, false));
        markerLinkageList.parallelStream()
            .forEach(markerLinkage -> {
                final Long fromId = markerLinkage.getFromId();
                final Long toId = markerLinkage.getToId();
                final String groupId = StrUtil.blankToDefault(markerLinkage.getGroupId(), "");
                if(StrUtil.isBlank(groupId)) {
                    return;
                }
                markerLinkageMap.putIfAbsent(fromId, groupId);
                markerLinkageMap.putIfAbsent(toId, groupId);
            });
    }

    /**
     * 返回点位分页压缩文档
     *
     * @param flagList 权限标记
     * @param md5 压缩文档数据的MD5
     * @return 压缩后的字节数组
     */
    @Override
    public byte[] getMarkerBinary(List<Integer> flagList, String md5) {
        try {
            if (StrUtil.isBlank(md5))
                throw new GenshinApiException("MD5不能为空");

            final Cache binaryCache = getMarkerBinaryCache();
            final Map<MarkerListCacheKey, String> binaryMd5Map = getMarkerMd5ByFlags(flagList);
            if (!binaryMd5Map.containsValue(md5))
                throw new GenshinApiException("分页数据未生成或超出获取范围");
            final byte[] binaryData = binaryCache.get(md5, byte[].class);
            if (binaryData == null)
                throw new GenshinApiException("分页数据未生成或超出获取范围");

            return binaryData;
        } catch (Exception e) {
            if (e instanceof GenshinApiException) {
                throw new GenshinApiException(e.getMessage());
            } else {
                throw new GenshinApiException("获取分页数据失败", e);
            }
        }
    }

    /**
     * 返回MD5列表
     *
     * @param flagList 权限标记
     * @return 过滤后的MD5数组
     */
    @Override
    public List<String> listMarkerBinaryMD5(List<Integer> flagList) {
        final Map<MarkerListCacheKey, String> binaryMd5Map = getMarkerMd5ByFlags(flagList);
        final LinkedHashMap<MarkerListCacheKey, String> binaryMd5MapSorted = sortMarkerMd5Map(binaryMd5Map);
        return new ArrayList<>(binaryMd5MapSorted.values());
    }

    /**
     * 刷新并返回点位分页压缩文档
     * @return 刷新后的各个分页
     */
    @Override
    public Map<String, byte[]> refreshMarkerBinaryList() {
        try {
            Cache binaryCache = getMarkerBinaryCache();
            Cache binaryMd5Cache = getMarkerBinaryMd5Cache();
            TimeInterval timer = DateUtil.timer();

            // 创建总缓存与映射关系
            timer.restart();
            final Map<Long, Item> itemMap = new HashMap<>();
            final ConcurrentHashMap<Long, List<MarkerItemLinkVo>> markerItemLinkMap = new ConcurrentHashMap<>();
            final ConcurrentHashMap<Long, String> markerLinkageMap = new ConcurrentHashMap<>();
            final Map<Integer, List<MarkerVo>> markerGroups = getMarkerVoGroups(itemMap, markerItemLinkMap, markerLinkageMap);
            log.info("[binary][marker] group creation, cost: {}", timer.intervalPretty());

            // 创建缓存数据
            final Map<String, byte[]> binaryMap = new HashMap<>();
            final Map<MarkerListCacheKey, String> binaryMd5Map = new HashMap<>();
            for (HiddenFlagEnum flagEnum : HiddenFlagEnum.values()) {
                final Integer flag = flagEnum.getCode();

                // 获取点位列表
                timer.restart();
                final List<MarkerVo> markerList = markerGroups.getOrDefault(flagEnum.getCode(), List.of());
                if (CollUtil.isEmpty(markerList))
                    continue;
                log.info("[binary][marker] marker list fetched from group, cost: {}, hiddenFlag: {}", timer.intervalPretty(), flag);

                // 切割点位列表
                timer.restart();
                final CacheSplitterEnum cacheSplitterEnum = CacheSplitterEnum.findSplitter(flagEnum);
                if (cacheSplitterEnum == null)
                    continue;
                final Function<List<MarkerVo>, Map<MarkerListCacheKey, List<MarkerVo>>> cacheSplitter = cacheSplitterEnum.getMarkerSplitter();
                if (cacheSplitter == null)
                    continue;
                final Map<MarkerListCacheKey, List<MarkerVo>> markerShards = cacheSplitter.apply(markerList);
                log.info("[binary][marker] marker list sliced, cost: {}, hiddenFlag: {}", timer.intervalPretty(), flag);

                // 插入点位压缩数据和MD5值
                timer.restart();
                for (Map.Entry<MarkerListCacheKey, List<MarkerVo>> markerShard : markerShards.entrySet()) {
                    final List<MarkerVo> cacheShardList = ObjUtil.defaultIfNull(markerShard.getValue(), List.of());
                    final byte[] cacheBinary = JSON.toJSONString(cacheShardList).getBytes(StandardCharsets.UTF_8);
                    final byte[] cacheBinaryCompressed = CompressUtils.compress(cacheBinary);
                    final String cacheBinaryMd5 = DigestUtils.md5DigestAsHex(cacheBinaryCompressed);
                    final MarkerListCacheKey cacheKey = markerShard.getKey()
                        .withHiddenFlag(flag)
                        .withMd5(cacheBinaryMd5);
                    binaryMap.put(cacheBinaryMd5, cacheBinaryCompressed);
                    binaryMd5Map.put(cacheKey, cacheBinaryMd5);
                }
                log.info("[binary][marker] marker shards updated, cost: {}, hiddenFlag: {}", timer.intervalPretty(), flag);
            }

            // 更新缓存数据
            timer.restart();
            binaryCache.clear();
            binaryMap.forEach(binaryCache::put);
            binaryMd5Cache.clear();
            binaryMd5Cache.put("", binaryMd5Map);
            log.info("[binary][marker] marker cache updated, cost: {}", timer.intervalPretty());

            return binaryMap;
        } catch (Exception e) {
            throw new GenshinApiException("创建压缩失败", e);
        }
    }

    private Cache getMarkerBinaryCache() {
        final Cache binaryCache = neverRefreshCacheManager.getCache(MarkerCacheKeyConst.MARKER_LIST_BIN_INDEXED);
        if (binaryCache == null)
            throw new GenshinApiException("缓存未初始化");
        return binaryCache;
    }

    private Cache getMarkerBinaryMd5Cache() {
        final Cache binaryMd5Cache = neverRefreshCacheManager.getCache(MarkerCacheKeyConst.MARKER_LIST_BIN_MD5);
        if (binaryMd5Cache == null)
            throw new GenshinApiException("缓存未初始化");
        return binaryMd5Cache;
    }

    private Map<MarkerListCacheKey, String> getMarkerMd5ByFlags(List<Integer> flagList) {
        final Map<MarkerListCacheKey, String> result = new HashMap<>();
        if (CollUtil.isEmpty(flagList))
            return result;

        final Cache binaryMd5Cache = getMarkerBinaryMd5Cache();
        final Cache.ValueWrapper binaryMd5Data = binaryMd5Cache.get("");
        final Map<MarkerListCacheKey, String> binaryMd5Map = binaryMd5Data == null ? new HashMap<>() : ((Map<MarkerListCacheKey, String>) binaryMd5Data.get());
        final Set<Integer> flagSet = new HashSet<>(flagList);
        for (Map.Entry<MarkerListCacheKey, String> binaryMd5Entry : binaryMd5Map.entrySet()) {
            final MarkerListCacheKey key = binaryMd5Entry.getKey();
            final String val = binaryMd5Entry.getValue();
            if (flagSet.contains(key.getHiddenFlag()))
                result.put(key, val);
        }
        return result;
    }

    private LinkedHashMap<MarkerListCacheKey, String> sortMarkerMd5Map(Map<MarkerListCacheKey, String> md5Map) {
        final List<Map.Entry<MarkerListCacheKey, String>> md5Entries = new ArrayList<>(md5Map.entrySet());
        final List<Map.Entry<MarkerListCacheKey, String>> md5EntriesSorted = md5Entries.stream()
            .sorted((a, b) -> {
                final MarkerListCacheKey aKey = a.getKey();
                final MarkerListCacheKey bKey = b.getKey();
                if (!ObjUtil.equal(aKey.getHiddenFlag(), bKey.getHiddenFlag()))
                    return aKey.getHiddenFlag() - bKey.getHiddenFlag();
                else if (!ObjUtil.equal(aKey.getIndex(), bKey.getIndex()))
                    return aKey.getIndex() - bKey.getIndex();
                else
                    return 0;
            })
            .collect(Collectors.toList());
        final LinkedHashMap<MarkerListCacheKey, String> md5MapSorted = new LinkedHashMap<>();
        md5EntriesSorted.forEach(entry -> {
            md5MapSorted.put(entry.getKey(), entry.getValue());
        });
        return md5MapSorted;
    }

    private Map<Integer, List<MarkerVo>> getMarkerVoGroups(
        Map<Long, Item> itemMap,
        ConcurrentHashMap<Long, List<MarkerItemLinkVo>> markerItemLinkMap,
        ConcurrentHashMap<Long, String> markerLinkageMap
    ) {
        TimeInterval timer = DateUtil.timer();

        // 获取覆盖标记映射
        timer.restart();
        final Map<Integer, Set<Integer>> overrideFlagMap = Arrays.stream(HiddenFlagEnum.values()).collect(Collectors.toMap(
            HiddenFlagEnum::getCode,
            v -> HiddenFlagEnum.getOverrideFlagList(v.getCode()),
            (o, n) -> n
        ));
        log.info("[binary][marker] marker flag override map generation, cost: {}", timer.intervalPretty());

        // 获取点位和点位相关映射数据
        timer.restart();
        final List<Marker> markerList = markerMapper.selectList(Wrappers.<Marker>lambdaQuery());
        final List<Long> markerIdList = markerList.stream().map(Marker::getId).distinct().collect(Collectors.toList());
        generateMarkerItemInfo(markerIdList, itemMap, markerItemLinkMap);
        generateMarkerLinkageInfo(markerIdList, markerLinkageMap);
        // 获取点位合并所需数据
        final List<Area> areaList = areaMapper.selectList(Wrappers.<Area>lambdaQuery().select(Area::getId, Area::getParentId, Area::getHiddenFlag, Area::getIsFinal));
        final Map<Long, Long> areaParentMap = areaList.stream().collect(Collectors.toMap(Area::getId, Area::getParentId, (o, n) -> n));
        final Map<Boolean, List<Area>> areaPartition = areaList.stream().collect(Collectors.partitioningBy(Area::getIsFinal));
        final Map<Long, Integer> areaFinalFlagMap = areaPartition.getOrDefault(true, List.of())
            .stream()
            .collect(Collectors.toMap(Area::getId, Area::getHiddenFlag, (o, n) -> n));
        final Map<Long, Integer> areaNonFinalFlagMap = areaPartition.getOrDefault(false, List.of())
            .stream()
            .collect(Collectors.toMap(Area::getId, Area::getHiddenFlag, (o, n) -> n));
        log.info("[binary][marker] marker and related data prepare, cost: {}", timer.intervalPretty());

        // 组合数据
        timer.restart();
        Map<Integer, List<MarkerVo>> markerGroup = markerList
            .stream()
            .map(marker -> {
                final Long markerId = marker.getId();
                return new MarkerDto(marker)
                    .withItemList(
                        markerItemLinkMap.getOrDefault(markerId, List.of())
                            .stream()
                            .sorted(Comparator.comparingLong(MarkerItemLinkVo::getItemId))
                            .collect(Collectors.toList())
                    )
                    .withLinkageId(markerLinkageMap.getOrDefault(markerId, ""))
                    .getVo();
            })
            .sorted(Comparator.comparingLong(MarkerVo::getId))
            .collect(Collectors.groupingBy(marker -> {
                Integer flag = marker.getHiddenFlag();
                final Long markerId = marker.getId();
                final List<MarkerItemLinkVo> markerItemLinkList = CollUtil.emptyIfNull(marker.getItemList());
                for (MarkerItemLinkVo itemLink : markerItemLinkList) {
                    // 覆盖物品标记
                    final Item linkedItem = itemMap.getOrDefault(itemLink.getItemId(), new Item());
                    final Integer linkedItemFlag = ObjUtil.defaultIfNull(linkedItem.getHiddenFlag(), -1);
                    final Set<Integer> linkedItemCanOverrideFlag = overrideFlagMap.getOrDefault(linkedItemFlag, new HashSet<>());
                    if (linkedItemCanOverrideFlag.contains(flag))
                        flag = linkedItemFlag;
                    // 覆盖地区标记
                    Long linkedAreaId = 0L;
                    Integer linkedAreaFlag = -1;
                    Set<Integer> linkedAreaCanOverrideFlag = new HashSet<>();
                    // 覆盖二级地区标记
                    linkedAreaId = linkedItem.getAreaId();
                    linkedAreaFlag = areaFinalFlagMap.getOrDefault(linkedAreaId, -1);
                    linkedAreaCanOverrideFlag = overrideFlagMap.getOrDefault(linkedAreaFlag, new HashSet<>());
                    if (linkedAreaCanOverrideFlag.contains(flag))
                        flag = linkedAreaFlag;
                    // 覆盖一级地区标记
                    linkedAreaId = areaParentMap.getOrDefault(linkedAreaId, 0L);
                    linkedAreaFlag = areaNonFinalFlagMap.getOrDefault(linkedAreaId, -1);
                    linkedAreaCanOverrideFlag = overrideFlagMap.getOrDefault(linkedAreaFlag, new HashSet<>());
                    if (linkedAreaCanOverrideFlag.contains(flag))
                        flag = linkedAreaFlag;
                }

                return flag;
            }));
        log.info("[binary][marker] marker composed, cost: {}", timer.intervalPretty());

        return markerGroup;
    }
}
