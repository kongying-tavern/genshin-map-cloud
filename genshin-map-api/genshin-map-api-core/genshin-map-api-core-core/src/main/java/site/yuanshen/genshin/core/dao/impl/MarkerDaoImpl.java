package site.yuanshen.genshin.core.dao.impl;

import cn.hutool.core.collection.CollUtil;
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
     * 通过bz2返回点位分页
     *
     * @param index 下标（从1开始）
     * @return 压缩后的字节数组
     */
    @Override
    @Cacheable(value = "listPageMarkerByBz2", cacheManager = "neverRefreshCacheManager")
    public byte[] listPageMarkerByBz2(Integer index) {
        throw new GenshinApiException("缓存未创建或超出索引范围");
    }

    /**
     * 刷新bz2返回点位分页
     *
     * @return 刷新后的各个分页
     */
    @Override
    public Map<String, byte[]> refreshPageMarkerByBz2() {
        try {
            Cache bz2Cache = neverRefreshCacheManager.getCache("listPageMarkerByBz2");
            if (bz2Cache == null) throw new GenshinApiException("缓存未初始化");

            Map<String, byte[]> result = new LinkedHashMap<>();
            for(HiddenFlagEnum flagEnum : HiddenFlagEnum.values()) {
                List<MarkerVo> markerList = getAllMarkerVo(flagEnum.getCode());
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
                        bz2Cache.put(cacheKey, compress);
                    }
                } else {
                    int totalPages = (int) ((markerList.size() + chunkSize - 1) / chunkSize);
                    for (int i = 0; i < totalPages; i++) {
                        byte[] page = JSON.toJSONString(CollUtil.page(i + 1, chunkSize, markerList)).getBytes(StandardCharsets.UTF_8);
                        byte[] compress = CompressUtils.compress(page);
                        String cacheKey = flagEnum.getCode() + "_" + i;
                        result.put(cacheKey, compress);
                        bz2Cache.put(cacheKey, compress);
                    }
                }
            }

            return result;
        } catch (Exception e) {
            throw new GenshinApiException("创建压缩失败", e);
        }
    }

    private List<MarkerVo> getAllMarkerVo(Integer hiddenFlag) {
        List<Integer> overrideFlags = HiddenFlagEnum.getOverrideList(hiddenFlag);
        overrideFlags = CollUtil.isEmpty(overrideFlags) ? List.of(hiddenFlag) : overrideFlags;

        Set<Long> areaIds = areaMapper.selectList(Wrappers.<Area>lambdaQuery().select(Area::getId).eq(Area::getHiddenFlag, hiddenFlag))
                .stream().map(Area::getId).collect(Collectors.toSet());
        Map<Long, Item> itemMap = CollUtil.isEmpty(areaIds) ? new HashMap<>() : itemMapper.selectWithLargeCustomIn(
                        "area_id",
                        PgsqlUtils.unnestLongStr(areaIds),
                        Wrappers.<Item>lambdaQuery().select(Item::getId).in(Item::getHiddenFlag, overrideFlags)
                )
                .stream().collect(Collectors.toMap(
                        Item::getId,
                        item -> item,
                        (o, n) -> n
                ));
        Set<Long> itemIds = itemMap.keySet();
        List<MarkerItemLink> markerItemLinks = CollUtil.isEmpty(itemIds) ? List.of() : new ArrayList<>(markerItemLinkMapper.selectWithLargeCustomIn(
                "item_id",
                PgsqlUtils.unnestLongStr(itemIds),
                Wrappers.<MarkerItemLink>lambdaQuery().select(MarkerItemLink::getItemId, MarkerItemLink::getMarkerId)
        ));
        Set<Long> markerIds = markerItemLinks.stream().map(MarkerItemLink::getMarkerId).collect(Collectors.toSet());
        List<Marker> markerList = CollUtil.isEmpty(markerIds) ? List.of() : markerMapper.selectListWithLargeIn(
                PgsqlUtils.unnestLongStr(markerIds),
                Wrappers.<Marker>lambdaQuery().in(Marker::getHiddenFlag, overrideFlags)
        );
        Set<Long> markerListIds = markerList.parallelStream().map(Marker::getId).collect(Collectors.toSet());

        // 合并点位-物品关联数据
        ConcurrentHashMap<Long, List<MarkerItemLink>> itemLinkMap = new ConcurrentHashMap<>();
        markerItemLinks.parallelStream().forEach(markerItemLink -> {
            if (!itemMap.containsKey(markerItemLink.getItemId())) {
                return;
            } else if(!markerListIds.contains(markerItemLink.getMarkerId())) {
                return;
            }
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

        // 获取点位管理组关联
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

        return markerList.parallelStream()
                .map(MarkerDto::new)
                .map(dto -> dto
                        .withItemList(
                                itemLinkMap.getOrDefault(dto.getId(), Collections.emptyList()).stream()
                                        .sorted(Comparator.comparingLong(MarkerItemLink::getId))
                                        .map(MarkerItemLinkDto::new)
                                        .map(MarkerItemLinkDto::getVo)
                                        .map(vo->{
                                            Long itemId = vo.getItemId();
                                            if (itemMap.containsKey(itemId))
                                                return vo.withIconTag(itemMap.get(itemId).getIconTag());
                                            else log.error("点位关联物品缺失:{}", itemId);
                                            return null;
                                        })
                                        .filter(Objects::nonNull)
                                        .collect(Collectors.toList())
                        )
                        .withLinkageId(markerLinkageMap.getOrDefault(dto.getId(), ""))
                )
                .map(MarkerDto::getVo)
                .collect(Collectors.toList());
    }
}
