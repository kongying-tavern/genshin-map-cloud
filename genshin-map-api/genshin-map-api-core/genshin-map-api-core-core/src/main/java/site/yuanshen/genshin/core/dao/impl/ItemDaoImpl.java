package site.yuanshen.genshin.core.dao.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.date.TimeInterval;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson2.JSON;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;
import site.yuanshen.common.core.exception.GenshinApiException;
import site.yuanshen.common.core.utils.CompressUtils;
import site.yuanshen.common.core.utils.JsonUtils;
import site.yuanshen.common.core.utils.PgsqlUtils;
import site.yuanshen.data.dto.ItemDto;
import site.yuanshen.data.entity.*;
import site.yuanshen.data.enums.HiddenFlagEnum;
import site.yuanshen.data.enums.cache.CacheSplitterEnum;
import site.yuanshen.data.mapper.*;
import site.yuanshen.data.vo.ItemVo;
import site.yuanshen.data.vo.adapter.cache.ItemCacheKeyConst;
import site.yuanshen.data.vo.adapter.cache.ItemListCacheKey;
import site.yuanshen.genshin.core.dao.ItemDao;

import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 物品信息的数据查询层实现
 *
 * @author Moment
 */
@Service
@Slf4j
public class ItemDaoImpl implements ItemDao {

    private final MarkerMapper markerMapper;
    private final MarkerItemLinkMapper markerItemLinkMapper;
    private final ItemMapper itemMapper;
    private final ItemTypeLinkMapper itemTypeLinkMapper;
    private final AreaMapper areaMapper;
    private final CacheManager neverRefreshCacheManager;

    @Autowired
    public ItemDaoImpl(MarkerMapper markerMapper,
                       MarkerItemLinkMapper markerItemLinkMapper,
                       ItemMapper itemMapper,
                       ItemTypeLinkMapper itemTypeLinkMapper,
                       AreaMapper areaMapper,
                       @Qualifier("neverRefreshCacheManager")
                       CacheManager neverRefreshCacheManager) {
        this.markerMapper = markerMapper;
        this.markerItemLinkMapper = markerItemLinkMapper;
        this.itemMapper = itemMapper;
        this.itemTypeLinkMapper = itemTypeLinkMapper;
        this.areaMapper = areaMapper;
        this.neverRefreshCacheManager = neverRefreshCacheManager;
    }

    /**
     * 生成物品点位相关信息
     *
     * @param itemIdList 物品ID列表
     * @param itemTypeIdMap 物品类型Map key:item_id, value:item_type_id[]
     * @param itemCountMap 物品计数Map key:item_id, value:{hiddenFlag: count}
     */
    @Override
    public void generateItemMarkerInfo(
        List<Long> itemIdList,
        ConcurrentMap<Long, List<Long>> itemTypeIdMap,
        ConcurrentMap<Long, Map<Integer, Integer>> itemCountMap
    ) {
        if (CollUtil.isEmpty(itemIdList))
            return;
        // 获取物品数据
        final List<MarkerItemLink> markerItemLinkList = markerItemLinkMapper.selectWithLargeCustomIn("item_id", PgsqlUtils.unnestLongStr(itemIdList), Wrappers.<MarkerItemLink>lambdaQuery().eq(MarkerItemLink::getDelFlag, false));
        final List<ItemTypeLink> itemTypeLinkList = itemTypeLinkMapper.selectWithLargeCustomIn("item_id", PgsqlUtils.unnestLongStr(itemIdList), Wrappers.<ItemTypeLink>lambdaQuery().eq(ItemTypeLink::getDelFlag, false));
        // 获取点位数据
        final List<Marker> markerList = markerMapper.selectList(Wrappers.<Marker>lambdaQuery().eq(Marker::getDelFlag, false).select(Marker::getId, Marker::getHiddenFlag));
        final Map<Long, Integer> markerFlagMap = markerList
            .parallelStream()
            .collect(Collectors.toMap(Marker::getId, Marker::getHiddenFlag, (o, n) -> n));
        // 汇总 item_id -> 物品类型ID映射
        itemTypeLinkList.forEach(itemTypeLink -> {
            itemTypeIdMap.compute(itemTypeLink.getItemId(),
                (itemId, typeIdList) -> {
                    if (typeIdList == null)
                        return new ArrayList<>(Collections.singletonList(itemTypeLink.getTypeId()));
                    typeIdList.add(itemTypeLink.getTypeId());
                    return typeIdList;
                }
            );
        });
        // 汇总 item_id -> 物品计数映射
        markerItemLinkList.forEach(markerItemLink -> {
            itemCountMap.compute(markerItemLink.getItemId(),
                (itemId, countSplitMap) -> {
                    final Long markerId = markerItemLink.getMarkerId();
                    final Integer itemCount = ObjUtil.defaultIfNull(markerItemLink.getCount(), 0);
                    final Integer markerFlag = markerFlagMap.getOrDefault(markerId, -1);
                    if (countSplitMap == null)
                        return new HashMap<>(Collections.singletonMap(markerFlag, itemCount));
                    final Integer itemCountValue = ObjUtil.defaultIfNull(countSplitMap.get(markerFlag), 0);
                    countSplitMap.put(markerFlag, itemCount + itemCountValue);
                    return countSplitMap;
                }
            );
        });
    }

    /**
     * 返回物品分页压缩文档
     *
     * @param flagList 权限标记
     * @param md5 压缩文档数据的MD5
     * @return 压缩后的字节数组
     */
    @Override
    public byte[] getItemBinary(List<Integer> flagList, String md5) {
        try {
            if (StrUtil.isBlank(md5))
                throw new GenshinApiException("MD5不能为空");

            final Cache binaryCache = getItemBinaryCache();
            final Map<ItemListCacheKey, String> binaryMd5Map = getItemMd5ByFlags(flagList);
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
    public List<String> listItemBinaryMD5(List<Integer> flagList) {
        final Map<ItemListCacheKey, String> binaryMd5Map = getItemMd5ByFlags(flagList);
        return new ArrayList<>(binaryMd5Map.values());
    }

    /**
     * 刷新物品压缩缓存并返回压缩文档
     *
     * @return 刷新后的各个分页
     */
    @Override
    public Map<String, byte[]> refreshItemBinaryList() {
        try {
            Cache binaryCache = getItemBinaryCache();
            Cache binaryMd5Cache = getItemBinaryMd5Cache();
            TimeInterval timer = DateUtil.timer();

            // 创建总缓存与映射关系
            timer.restart();
            final ConcurrentHashMap<Long, List<Long>> itemTypeIdMap = new ConcurrentHashMap<>();
            final ConcurrentHashMap<Long, Map<Integer, Integer>> itemCountMap = new ConcurrentHashMap<>();
            final Map<Integer, List<ItemVo>> itemGroups = getItemVoGroups(itemTypeIdMap, itemCountMap);
            log.info("[binary][item] group creation, cost: {}", timer.intervalPretty());

            // 创建缓存数据
            final Map<String, byte[]> binaryMap = new HashMap<>();
            final Map<ItemListCacheKey, String> binaryMd5Map = new HashMap<>();
            for (HiddenFlagEnum flagEnum : HiddenFlagEnum.values()) {
                final Integer flag = flagEnum.getCode();

                // 获取物品列表
                timer.restart();
                final List<ItemVo> itemList = itemGroups.getOrDefault(flagEnum.getCode(), List.of());
                if (CollUtil.isEmpty(itemList))
                    continue;
                log.info("[binary][item] item list fetched from group, cost: {}, hiddenFlag: {}", timer.intervalPretty(), flag);

                // 切割物品列表
                timer.restart();
                final CacheSplitterEnum cacheSplitterEnum = CacheSplitterEnum.findSplitter(flagEnum);
                if (cacheSplitterEnum == null)
                    continue;
                final Function<List<ItemVo>, Map<ItemListCacheKey, List<ItemVo>>> cacheSplitter = cacheSplitterEnum.getItemSplitter();
                if (cacheSplitter == null)
                    continue;
                final Map<ItemListCacheKey, List<ItemVo>> itemShards = cacheSplitter.apply(itemList);
                log.info("[binary][item] item list sliced, cost: {}, hiddenFlag: {}", timer.intervalPretty(), flag);

                // 插入点位压缩数据和MD5值
                timer.restart();
                for (Map.Entry<ItemListCacheKey, List<ItemVo>> itemShard : itemShards.entrySet()) {
                    final List<ItemVo> cacheShardList = ObjUtil.defaultIfNull(itemShard.getValue(), List.of());
                    final byte[] cacheBinary = JSON.toJSONString(cacheShardList, JsonUtils.defaultWriteFeatures).getBytes(StandardCharsets.UTF_8);
                    final byte[] cacheBinaryCompressed = CompressUtils.compress(cacheBinary);
                    final String cacheBinaryMd5 = DigestUtils.md5DigestAsHex(cacheBinaryCompressed);
                    final ItemListCacheKey cacheKey = itemShard.getKey()
                        .withHiddenFlag(flag)
                        .withMd5(cacheBinaryMd5);
                    binaryMap.put(cacheBinaryMd5, cacheBinaryCompressed);
                    binaryMd5Map.put(cacheKey, cacheBinaryMd5);
                }
                log.info("[binary][item] item shards updated, cost: {}, hiddenFlag: {}", timer.intervalPretty(), flag);
            }

            // 更新缓存数据
            timer.restart();
            binaryCache.clear();
            binaryMap.forEach(binaryCache::put);
            binaryMd5Cache.clear();
            binaryMd5Cache.put("", binaryMd5Map);
            log.info("[binary][item] item cache updated, cost: {}", timer.intervalPretty());

            return binaryMap;
        } catch (Exception e) {
            throw new GenshinApiException("创建压缩失败", e);
        }
    }

    private Cache getItemBinaryCache() {
        final Cache binaryCache = neverRefreshCacheManager.getCache(ItemCacheKeyConst.ITEM_LIST_BIN_INDEXED);
        if (binaryCache == null)
            throw new GenshinApiException("缓存未初始化");
        return binaryCache;
    }

    private Cache getItemBinaryMd5Cache() {
        final Cache binaryMd5Cache = neverRefreshCacheManager.getCache(ItemCacheKeyConst.ITEM_LIST_BIN_MD5);
        if (binaryMd5Cache == null)
            throw new GenshinApiException("缓存未初始化");
        return binaryMd5Cache;
    }

    private Map<ItemListCacheKey, String> getItemMd5ByFlags(List<Integer> flagList) {
        final Map<ItemListCacheKey, String> result = new HashMap<>();
        if (CollUtil.isEmpty(flagList))
            return result;

        final Cache binaryMd5Cache = getItemBinaryMd5Cache();
        final Cache.ValueWrapper binaryMd5Data = binaryMd5Cache.get("");
        final Map<ItemListCacheKey, String> binaryMd5Map = binaryMd5Data == null ? new HashMap<>() : ((Map<ItemListCacheKey, String>) binaryMd5Data.get());
        final Set<Integer> flagSet = new HashSet<>(flagList);
        for (Map.Entry<ItemListCacheKey, String> binaryMd5Entry : binaryMd5Map.entrySet()) {
            final ItemListCacheKey key = binaryMd5Entry.getKey();
            final String val = binaryMd5Entry.getValue();
            if (flagSet.contains(key.getHiddenFlag()))
                result.put(key, val);
        }
        return result;
    }

    private Map<Integer, List<ItemVo>> getItemVoGroups(
        ConcurrentMap<Long, List<Long>> itemTypeIdMap,
        ConcurrentMap<Long, Map<Integer, Integer>> itemCountMap
    ) {
        TimeInterval timer = DateUtil.timer();

        // 获取覆盖标记映射
        timer.restart();
        final Map<Integer, Set<Integer>> overrideFlagMap = Arrays.stream(HiddenFlagEnum.values()).collect(Collectors.toMap(
            HiddenFlagEnum::getCode,
            v -> HiddenFlagEnum.getOverrideFlagList(v.getCode()),
            (o, n) -> n
        ));
        log.info("[binary][item] item flag override map generation, cost: {}", timer.intervalPretty());

        // 获取物品和物品相关映射数据
        timer.restart();
        final List<Item> itemList = itemMapper.selectList(Wrappers.<Item>lambdaQuery().eq(Item::getDelFlag, false));
        final List<Long> itemIdList = itemList.stream().map(Item::getId).distinct().collect(Collectors.toList());
        generateItemMarkerInfo(itemIdList, itemTypeIdMap, itemCountMap);
        // 获取物品合并所需数据
        final List<Area> areaList = areaMapper.selectList(Wrappers.<Area>lambdaQuery().eq(Area::getDelFlag, false).select(Area::getId, Area::getParentId, Area::getHiddenFlag, Area::getIsFinal));
        final Map<Long, Long> areaParentMap = areaList.stream().collect(Collectors.toMap(Area::getId, Area::getParentId, (o, n) -> n));
        final Map<Boolean, List<Area>> areaPartition = areaList.stream().collect(Collectors.partitioningBy(Area::getIsFinal));
        final Map<Long, Integer> areaFinalFlagMap = areaPartition.getOrDefault(true, List.of())
            .stream()
            .collect(Collectors.toMap(Area::getId, Area::getHiddenFlag, (o, n) -> n));
        final Map<Long, Integer> areaNonFinalFlagMap = areaPartition.getOrDefault(false, List.of())
            .stream()
            .collect(Collectors.toMap(Area::getId, Area::getHiddenFlag, (o, n) -> n));
        log.info("[binary][item] item and related data prepare, cost: {}", timer.intervalPretty());

        // 组合数据
        timer.restart();
        final Map<Long, Integer> itemOverrideFlagMap = new HashMap<>();
        Map<Integer, List<ItemVo>> itemGroup = itemList
            .stream()
            .map(item -> {
                final Long itemId = item.getId();
                Integer flag = item.getHiddenFlag();
                // 覆盖地区标记
                Long linkedAreaId = 0L;
                Integer linkedAreaFlag = -1;
                Set<Integer> linkedAreaCanOverrideFlag = new HashSet<>();
                // 覆盖二级地区标记
                linkedAreaId = item.getAreaId();
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
                // 暂存覆盖标记，供后续分组使用
                itemOverrideFlagMap.put(itemId, flag);
                // 制作物品计数数据
                final Set<Integer> itemCountableFlagSet = overrideFlagMap.getOrDefault(flag, new HashSet<>());
                final LinkedHashMap<Integer, Integer> itemCountSplit = itemCountMap.getOrDefault(itemId, new HashMap<>())
                    .entrySet()
                    .stream()
                    .filter(v -> itemCountableFlagSet.contains(v.getKey()))
                    .sorted(Comparator.comparingInt(Map.Entry::getKey))
                    .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (o, n) -> o, LinkedHashMap::new));
                final Integer itemCount = itemCountSplit.values().stream().reduce(0, Integer::sum);

                return new ItemDto(item)
                    .withTypeIdList(
                        itemTypeIdMap.getOrDefault(itemId, List.of())
                            .stream()
                            .sorted(Comparator.comparingLong(v -> v))
                            .collect(Collectors.toList())
                    )
                    .withCount(itemCount)
                    .withCountSplit(itemCountSplit)
                    .getVo();
            })
            .sorted(Comparator.comparingLong(ItemVo::getId))
            .collect(Collectors.groupingBy(item -> itemOverrideFlagMap.getOrDefault(item.getId(), -1)));
        log.info("[binary][item] item composed, cost: {}", timer.intervalPretty());

        return itemGroup;
    }
}
