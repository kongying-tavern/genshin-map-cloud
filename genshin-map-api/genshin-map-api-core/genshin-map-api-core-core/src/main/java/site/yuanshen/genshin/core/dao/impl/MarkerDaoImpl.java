package site.yuanshen.genshin.core.dao.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson2.JSON;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import site.yuanshen.common.core.exception.GenshinApiException;
import site.yuanshen.common.core.utils.CompressUtils;
import site.yuanshen.common.core.utils.PgsqlUtils;
import site.yuanshen.data.dto.MarkerDto;
import site.yuanshen.data.dto.MarkerItemLinkDto;
import site.yuanshen.data.dto.helper.PageSearchDto;
import site.yuanshen.data.entity.*;
import site.yuanshen.data.enums.HiddenFlagEnum;
import site.yuanshen.data.mapper.*;
import site.yuanshen.data.vo.MarkerItemLinkVo;
import site.yuanshen.data.vo.MarkerVo;
import site.yuanshen.data.vo.helper.PageListVo;
import site.yuanshen.genshin.core.dao.MarkerDao;

import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
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

    @Data
    class MarkerHiddenFlagListPack<T> {
        private List<T> canOverride = new ArrayList<>();
        private List<T> mustExact = new ArrayList<>();
    }

    @Data
    class MarkerHiddenFlagMapPack<K, V> {
        private Map<K, V> canOverride = new HashMap<>();
        private Map<K, V> mustExact = new HashMap<>();
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

        ConcurrentHashMap<Long, List<MarkerItemLinkVo>> itemLinkMap = new ConcurrentHashMap<>();
        Map<Long, Item> itemMap = new HashMap<>();
        getAllItemRelateInfoById(markerIdList, itemLinkMap, itemMap);

        ConcurrentHashMap<Long, String> markerLinkageMap = new ConcurrentHashMap<>();
        getAllLinkageRelateInfoById(markerIdList, markerLinkageMap);

        return new PageListVo<MarkerVo>()
                .setRecord(markerPage.getRecords().parallelStream()
                        .map(marker -> new MarkerDto(marker)
                            .withItemList(itemLinkMap.get(marker.getId()))
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

        ConcurrentHashMap<Long, List<MarkerItemLinkVo>> itemLinkMap = new ConcurrentHashMap<>();
        Map<Long, Item> itemMap = new HashMap<>();
        getAllItemRelateInfoById(markerIdList, itemLinkMap, itemMap);

        ConcurrentHashMap<Long, String> markerLinkageMap = new ConcurrentHashMap<>();
        getAllLinkageRelateInfoById(markerIdList, markerLinkageMap);

        return markerList.parallelStream()
                        .map(marker -> new MarkerDto(marker)
                            .withItemList(itemLinkMap.get(marker.getId()))
                            .withLinkageId(markerLinkageMap.getOrDefault(marker.getId(), ""))
                            .getVo())
                        .collect(Collectors.toList());
    }

    /**
     * 根据点位ID查询对应的物品&物品关联
     * @param markerIdList 点位ID列表
     * @param itemLinkMap 物品链接Map  key:marker_id, value:marker_item_link
     * @param itemMap 物品链接Map  key:item_id, value:item
     */
    public void getAllItemRelateInfoById(List<Long> markerIdList, ConcurrentHashMap<Long, List<MarkerItemLinkVo>> itemLinkMap, Map<Long, Item> itemMap) {
        List<Long> itemIdList = markerItemLinkMapper.selectWithLargeCustomIn("marker_id", PgsqlUtils.unnestLongStr(markerIdList), Wrappers.lambdaQuery())
                .parallelStream().map(markerItemLink -> {
                    itemLinkMap.compute(markerItemLink.getMarkerId(),
                            (markerId, linkList) -> {
                                MarkerItemLinkVo vo = new MarkerItemLinkDto(markerItemLink).getVo();
                                if (linkList == null) return new ArrayList<>(Collections.singletonList(vo));
                                linkList.add(vo);
                                return linkList;
                            });
                    return markerItemLink.getItemId();
                })
                .distinct().collect(Collectors.toList());
        //获取item_id,得到item合集
        itemMap.putAll(
                itemMapper.selectListWithLargeIn(PgsqlUtils.unnestLongStr(itemIdList),Wrappers.lambdaQuery())
                .stream().collect(Collectors.toMap(Item::getId, Item -> Item))
        );
        itemLinkMap.forEach((markerId,linkVoList) ->
            linkVoList.forEach(link -> {
                final String iconTag = StrUtil.blankToDefault(itemMap.getOrDefault(link.getItemId(), new Item()).getIconTag(), "");
                link.setIconTag(iconTag);
            })
        );
    }

    public void getAllLinkageRelateInfoById(List<Long> markerIdList, ConcurrentHashMap<Long, String> markerLinkageMap) {
        if(CollUtil.isEmpty(markerIdList)) {
            return;
        }
        List<MarkerLinkage> markerLinkageList = markerLinkageMapper.selectWithLargeMarkerIdIn(PgsqlUtils.unnestLongStr(markerIdList), Wrappers.<MarkerLinkage>lambdaQuery().eq(MarkerLinkage::getDelFlag, false));
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
    public byte[] listPageMarkerByBinary(List<Integer> flagList, String md5) {
        try {
            if(StrUtil.isBlank(md5)) {
                throw new GenshinApiException("MD5不能为空");
            }
            // 查找MD5对应的Key
            LinkedHashMap<String, String> md5Map = getMarkerMd5ByFlags(flagList);
            String md5Key = "";
            for(Map.Entry<String, String> md5Entry : md5Map.entrySet()) {
                String key = md5Entry.getKey();
                String val = md5Entry.getValue();
                if(val != null && val.equals(md5)) {
                    md5Key = key;
                    break;
                }
            }
            if(StrUtil.isBlank(md5Key)) {
                throw new GenshinApiException("分页数据未生成或超出获取范围");
            }

            Cache binaryCache = neverRefreshCacheManager.getCache("listPageMarkerByBinary");
            if (binaryCache == null) throw new GenshinApiException("缓存未初始化");
            byte[] result = binaryCache.get(md5Key, byte[].class);
            if(result == null) throw new GenshinApiException("分页数据未生成或超出获取范围");
            return result;
        } catch (Exception e) {
            throw new GenshinApiException("获取分页数据失败", e);
        }
    }

    /**
     * 返回MD5列表
     *
     * @param flagList 权限标记
     * @return 过滤后的MD5数组
     */
    @Override
    public List<String> listMarkerMD5(List<Integer> flagList) {
        LinkedHashMap<String, String> md5Map = getMarkerMd5ByFlags(flagList);
        return new ArrayList<>(md5Map.values());
    }

    /**
     * 刷新并返回点位分页压缩文档
     * @return 刷新后的各个分页
     */
    @Override
    public Map<String, byte[]> refreshPageMarkerByBinary() {
        try {
            Cache binaryCache = neverRefreshCacheManager.getCache("listPageMarkerByBinary");
            if (binaryCache == null) throw new GenshinApiException("缓存未初始化");

            // 创建总缓存
            Map<Integer, List<MarkerVo>> markerGroups = getAllMarkerVoGroups();

            Map<String, byte[]> result = new LinkedHashMap<>();
            for(HiddenFlagEnum flagEnum : HiddenFlagEnum.values()) {
                List<MarkerVo> markerList = getAllMarkerVo(markerGroups, flagEnum.getCode());
                markerList.sort(Comparator.comparingLong(MarkerVo::getId));
                boolean chunkById = flagEnum.getChunkById();
                int chunkSize = flagEnum.getChunkSize();
                if (chunkById) {
                    Long lastId = markerList.get(markerList.size() - 1).getId();
                    int totalPages = (int) ((lastId + chunkSize - 1) / chunkSize);
                    for (int i = 0; i < totalPages; i++) {
                        int finalI = i;
                        byte[] page = JSON.toJSONString(
                                        markerList.parallelStream()
                                                .filter(markerVo -> markerVo.getId() >= (finalI * chunkSize) && markerVo.getId() < ((finalI + 1) * chunkSize))
                                                .sorted(Comparator.comparingLong(MarkerVo::getId)).collect(Collectors.toList()))
                                .getBytes(StandardCharsets.UTF_8);
                        byte[] compress = CompressUtils.compress(page);
                        String cacheKey = flagEnum.getCode() + "_" + i;
                        result.put(cacheKey, compress);
                        binaryCache.put(cacheKey, compress);
                    }
                } else {
                    int totalPages = (int) ((markerList.size() + chunkSize - 1) / chunkSize);
                    for (int i = 0; i < totalPages; i++) {
                        byte[] page = JSON.toJSONString(CollUtil.page(i, chunkSize, markerList)).getBytes(StandardCharsets.UTF_8);
                        byte[] compress = CompressUtils.compress(page);
                        String cacheKey = flagEnum.getCode() + "_" + i;
                        result.put(cacheKey, compress);
                        binaryCache.put(cacheKey, compress);
                    }
                }
            }

            return result;
        } catch (Exception e) {
            throw new GenshinApiException("创建压缩失败", e);
        }
    }

    private LinkedHashMap<String, String> getMarkerMd5ByFlags(List<Integer> flagList) {
        LinkedHashMap<String, String> result = new LinkedHashMap<>();
        if(CollUtil.isEmpty(flagList)) {
            return result;
        }
        Set<Integer> flagSet = new HashSet<>(flagList);

        Cache binaryCache = neverRefreshCacheManager.getCache("listMarkerBinaryMD5");
        if (binaryCache == null) throw new GenshinApiException("缓存未初始化");
        Map<String, String> md5Map = (Map<String, String>) binaryCache.get("").get();
        for(Map.Entry<String, String> md5Entry : md5Map.entrySet()) {
            if(md5Entry == null) {
                continue;
            }
            String key = md5Entry.getKey();
            String val = md5Entry.getValue();
            if(key == null || val == null) {
                continue;
            }
            String keyFlag = key.replaceAll("_\\d+", "");
            int keyFlagNum = 0;
            try {
                keyFlagNum = Integer.valueOf(keyFlag);
            } catch(Exception e) {
                continue;
            }
            if(flagSet.contains(keyFlagNum)) {
                result.put(key, val);
            }
        }
        return result;
    }

    private Map<Integer, List<MarkerVo>> getAllMarkerVoGroups() {
        Map<Integer, List<Integer>> overrideFlags = new HashMap<>();
        for(HiddenFlagEnum flagEnum : HiddenFlagEnum.values()) {
            List<Integer> overrides = HiddenFlagEnum.getOverrideList(flagEnum.getCode());
            overrides = CollUtil.isEmpty(overrides) ? List.of(flagEnum.getCode()) : overrides;
            overrideFlags.put(flagEnum.getCode(), overrides);
        }
        List<Area> areas = areaMapper.selectList(Wrappers.<Area>lambdaQuery().select(Area::getId, Area::getHiddenFlag));
        Map<Long, Integer> areaFlagMap = areas.stream().collect(Collectors.toMap(Area::getId, Area::getHiddenFlag, (o, n) -> n));
        List<Item> items = itemMapper.selectList(Wrappers.<Item>lambdaQuery().select(Item::getId, Item::getHiddenFlag, Item::getAreaId, Item::getIconTag));
        Map<Long, Long> itemAreaIdMap = items.stream().collect(Collectors.toMap(Item::getId, Item::getAreaId, (o, n) -> n));
        Map<Long, Integer> itemFlagMap = items.stream().collect(Collectors.toMap(Item::getId, Item::getHiddenFlag, (o, n) -> n));
        Map<Long, String> itemIconTagMap = items.stream().collect(Collectors.toMap(Item::getId, Item::getIconTag, (o, n) -> n));
        List<MarkerItemLink> markerItemLinks = markerItemLinkMapper.selectList(Wrappers.<MarkerItemLink>lambdaQuery());
        List<Marker> markers = markerMapper.selectList(Wrappers.<Marker>lambdaQuery());

        // 合并点位-物品关联数据
        ConcurrentHashMap<Long, List<MarkerItemLink>> itemLinkMap = new ConcurrentHashMap<>();
        markerItemLinks.parallelStream().forEach(markerItemLink -> {
            itemLinkMap.compute(
                    markerItemLink.getMarkerId(),
                    (markerId, linkList) -> {
                        if (linkList == null) {
                            return new ArrayList<>(Collections.singletonList(markerItemLink));
                        }
                        linkList.add(markerItemLink);
                        return linkList;
                    }
            );
        });

        // 获取点位关联
        ConcurrentHashMap<Long, String> markerLinkageMap = new ConcurrentHashMap<>();
        List<MarkerLinkage> markerLinkages = markerLinkageMapper.selectList(Wrappers.<MarkerLinkage>lambdaQuery());
        markerLinkages.parallelStream().forEach(markerLinkage -> {
                final String groupId = StrUtil.blankToDefault(markerLinkage.getGroupId(), "");
                if(StrUtil.isBlank(groupId)) {
                    return;
                }
                markerLinkageMap.putIfAbsent(markerLinkage.getFromId(), groupId);
                markerLinkageMap.putIfAbsent(markerLinkage.getToId(), groupId);
        });

        // 构造点位分组缓存，后续重新整理为点位分组
        ConcurrentHashMap<ImmutablePair<Integer, Long>, MarkerVo> markerCache = new ConcurrentHashMap<>();
        markers.parallelStream().forEach(m -> {
            if(m == null)
                return;

            // 判断层级标识
            Integer mFlag = m.getHiddenFlag();
            if(mFlag == null)
                return;
            List<MarkerItemLink> mItemLink = itemLinkMap.get(m.getId());
            if(CollUtil.isEmpty(mItemLink))
                return;
            mItemLink.forEach(itemLink -> {
                // 由于可能存在一个点位属于多个物品的情况，所以对于点位中不同物品进行分别处理
                Integer curFlag = mFlag;

                // 判断物品是否可以覆盖当前点位的隐藏标识
                Integer mItemFlag = itemFlagMap.get(itemLink.getItemId());
                if(mItemFlag == null)
                    return;
                if(overrideFlags.getOrDefault(mItemFlag, List.of()).contains(curFlag))
                    curFlag = mItemFlag;

                // 判断地区是否可以覆盖当前点位的隐藏标识
                Long mAreaId = itemAreaIdMap.get(itemLink.getItemId());
                if(mAreaId == null)
                    return;
                Integer mAreaFlag = areaFlagMap.get(mAreaId);
                if(mAreaFlag == null)
                    return;
                if(overrideFlags.getOrDefault(mAreaFlag, List.of()).contains(curFlag))
                    curFlag = mAreaFlag;

                // 将当前点位放入数据池
                markerCache.computeIfAbsent(
                        ImmutablePair.of(curFlag, m.getId()),
                        (flagIdPair) -> {
                            return new MarkerDto(m)
                                    .withItemList(
                                            itemLinkMap.getOrDefault(m.getId(), Collections.emptyList())
                                                    .stream()
                                                    .sorted(Comparator.comparingLong(MarkerItemLink::getId))
                                                    .map(MarkerItemLinkDto::new)
                                                    .map(MarkerItemLinkDto::getVo)
                                                    .map(vo -> vo.withIconTag(itemIconTagMap.get(vo.getItemId())))
                                                    .collect(Collectors.toList())
                                    )
                                    .withLinkageId(markerLinkageMap.getOrDefault(m.getId(), ""))
                                    .getVo();
                        }
                );
            });


        });

        // 构造点位分组
        ConcurrentHashMap<Integer, List<MarkerVo>> markerGroups = new ConcurrentHashMap<>();
        markerCache.forEach(1, (flagIdPair, markerVo) -> {
            Integer flag = flagIdPair.getLeft();
            markerGroups.compute(
                    flag,
                    (f, markerVoList) -> {
                        if (markerVoList == null) {
                            return new ArrayList<>(Collections.singletonList(markerVo));
                        }
                        markerVoList.add(markerVo);
                        return markerVoList;
                    }
            );
        });

        return markerGroups;
    }

    private List<MarkerVo> getAllMarkerVo(Map<Integer, List<MarkerVo>> groups, Integer hiddenFlag) {
        return groups.getOrDefault(hiddenFlag, List.of());
    }
}
