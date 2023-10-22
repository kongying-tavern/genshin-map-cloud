package site.yuanshen.genshin.core.dao.impl;

import com.alibaba.fastjson2.JSON;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.caffeine.CaffeineCache;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;
import site.yuanshen.common.core.exception.GenshinApiException;
import site.yuanshen.common.core.utils.CompressUtils;
import site.yuanshen.common.core.utils.PgsqlUtils;
import site.yuanshen.data.dto.ItemDto;
import site.yuanshen.data.entity.Item;
import site.yuanshen.data.entity.ItemTypeLink;
import site.yuanshen.data.entity.Marker;
import site.yuanshen.data.entity.MarkerItemLink;
import site.yuanshen.data.mapper.ItemMapper;
import site.yuanshen.data.mapper.ItemTypeLinkMapper;
import site.yuanshen.data.mapper.MarkerItemLinkMapper;
import site.yuanshen.data.mapper.MarkerMapper;
import site.yuanshen.data.vo.ItemVo;
import site.yuanshen.genshin.core.dao.ItemDao;

import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 物品信息的数据查询层实现
 *
 * @author Moment
 */
@Service
public class ItemDaoImpl implements ItemDao {

    private final ItemMapper itemMapper;
    private final ItemTypeLinkMapper itemTypeLinkMapper;
    private final MarkerItemLinkMapper markerItemLinkMapper;
    private final MarkerMapper markerMapper;

    @Autowired
    public ItemDaoImpl(ItemMapper itemMapper,
                       ItemTypeLinkMapper itemTypeLinkMapper,
                       MarkerItemLinkMapper markerItemLinkMapper,
                       MarkerMapper markerMapper) {
        this.itemMapper = itemMapper;
        this.itemTypeLinkMapper = itemTypeLinkMapper;
        this.markerItemLinkMapper = markerItemLinkMapper;
        this.markerMapper = markerMapper;
    }

    /**
     * @return 所有的物品信息
     */
    @Override
    @Cacheable("listAllItem")
    public List<ItemVo> listAllItem() {
        List<Item> itemList = itemMapper
                .selectList(
                        Wrappers.<Item>lambdaQuery()
                                .eq(Item::getHiddenFlag, 0));
        if (itemList.size() == 0)
            return new ArrayList<>();
        //获取分类数据
        List<ItemTypeLink> typeLinkList = itemTypeLinkMapper.selectWithLargeCustomIn("item_id", PgsqlUtils.unnestStr(itemList.stream()
                                .map(Item::getId).distinct().collect(Collectors.toList())),Wrappers.lambdaQuery());
        Map<Long, List<Long>> itemToTypeMap = new HashMap<>();
        for (ItemTypeLink typeLink : typeLinkList) {
            Long itemId = typeLink.getItemId();
            List<Long> typeList = itemToTypeMap.getOrDefault(itemId, new ArrayList<>());
            typeList.add(typeLink.getTypeId());
            itemToTypeMap.put(itemId, typeList);
        }

        //获取点位数据
        List<MarkerItemLink> markerItemLinkList = markerItemLinkMapper.selectWithLargeCustomIn("item_id", PgsqlUtils.unnestStr(itemList.stream()
                                .map(Item::getId).distinct().collect(Collectors.toList())),Wrappers.<MarkerItemLink>lambdaQuery());

        List<Long> markerIdList = markerMapper.selectList(Wrappers.<Marker>lambdaQuery()
                        .eq(Marker::getHiddenFlag, 0)
                        .select(Marker::getId))
                .parallelStream().map(Marker::getId).collect(Collectors.toList());
        //计算各个物品在点位中的数量合计
        Map<Long, Integer> markerItemLinkCount = new HashMap<>();
        markerItemLinkList.parallelStream()
                .filter(markerItemLink -> markerIdList.contains(markerItemLink.getMarkerId()))
                .collect(Collectors.groupingBy(MarkerItemLink::getItemId)).forEach(
                        (itemId, list) -> markerItemLinkCount.put(itemId, list.stream().mapToInt(MarkerItemLink::getCount).sum())
                );
        return itemList.stream()
                .map(ItemDto::new)
                .map(itemDto -> itemDto
                    .withCount(markerItemLinkCount.getOrDefault(itemDto.getId(), 0))
                    .withTypeIdList(itemToTypeMap.get(itemDto.getId())))
                .map(ItemDto::getVo)
                .sorted(Comparator.comparing(ItemVo::getSortIndex).reversed()).collect(Collectors.toList());
    }

    /**
     * @return 所有的物品信息的Bz2压缩
     */
    @Override
    @Cacheable(value = "listAllItemBz2", cacheManager = "neverRefreshCacheManager")
    public byte[] listAllItemBz2() {
        //通过refreshAllItemBz2()刷新失败
        throw new GenshinApiException("缓存未创建");
    }

    /**
     * 刷新物品压缩缓存并返回压缩文档
     *
     * @return 物品压缩文档
     */
    @Override
    @CachePut(value = "listAllItemBz2", cacheManager = "neverRefreshCacheManager")
    public byte[] refreshAllItemBz2() {
        try {
            List<ItemVo> itemList = listAllItem();
            byte[] result = JSON.toJSONString(itemList).getBytes(StandardCharsets.UTF_8);
            return CompressUtils.compress(result);
        } catch (Exception e) {
            throw new GenshinApiException("创建压缩失败", e);
        }
    }
}
