package site.yuanshen.genshin.core.dao.impl;

import com.alibaba.fastjson2.JSON;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.caffeine.CaffeineCache;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;
import site.yuanshen.common.core.utils.CompressUtils;
import site.yuanshen.data.dto.ItemDto;
import site.yuanshen.data.entity.Item;
import site.yuanshen.data.entity.ItemTypeLink;
import site.yuanshen.data.entity.Marker;
import site.yuanshen.data.entity.MarkerItemLink;
import site.yuanshen.data.mapper.*;
import site.yuanshen.data.vo.ItemVo;
import site.yuanshen.genshin.core.dao.ItemDao;
import site.yuanshen.genshin.core.service.CacheService;
import site.yuanshen.genshin.core.service.mbp.ItemAreaPublicMBPService;
import site.yuanshen.genshin.core.service.mbp.ItemMBPService;
import site.yuanshen.genshin.core.service.mbp.ItemTypeLinkMBPService;
import site.yuanshen.genshin.core.service.mbp.ItemTypeMBPService;

import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 物品信息的数据查询层实现
 *
 * @author Moment
 */
@Service
@RequiredArgsConstructor
public class ItemDaoImpl implements ItemDao {

    private final CacheManager cacheManager;
    private final ItemMapper itemMapper;
    private final ItemTypeLinkMapper itemTypeLinkMapper;
    private final MarkerItemLinkMapper markerItemLinkMapper;
    private final MarkerMapper markerMapper;

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
        List<ItemTypeLink> typeLinkList = itemTypeLinkMapper.selectList(Wrappers.<ItemTypeLink>lambdaQuery()
                .in(ItemTypeLink::getItemId,
                        itemList.stream()
                                .map(Item::getId).distinct().collect(Collectors.toList())));
        Map<Long, List<Long>> itemToTypeMap = new HashMap<>();
        for (ItemTypeLink typeLink : typeLinkList) {
            Long itemId = typeLink.getItemId();
            List<Long> typeList = itemToTypeMap.getOrDefault(itemId, new ArrayList<>());
            typeList.add(typeLink.getTypeId());
            itemToTypeMap.put(itemId, typeList);
        }

        //获取点位数据
        List<MarkerItemLink> markerItemLinkList = markerItemLinkMapper.selectList(Wrappers.<MarkerItemLink>lambdaQuery()
                .in(MarkerItemLink::getItemId,
                        itemList.stream()
                                .map(Item::getId).distinct().collect(Collectors.toList())));
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
                .map(itemDto -> itemDto.withTypeIdList(itemToTypeMap.get(itemDto.getId())))
                .map(ItemDto::getVo)
                .sorted(Comparator.comparing(ItemVo::getSortIndex).reversed()).collect(Collectors.toList());
    }

    /**
     * @return 所有的物品信息的Bz2压缩
     */
    @Override
    @Cacheable(value = "listAllItemBz2", key = "'allItemBz2'")
    public byte[] listAllItemBz2() {
        try {
            return CompressUtils.compress(JSON.toJSONString(
                            listAllItem())
                    .getBytes(StandardCharsets.UTF_8));
        } catch (Exception e) {
            throw new RuntimeException("创建压缩失败" + e);
        }
    }

    /**
     * @return 所有的物品信息的Bz2压缩的md5
     */
    @Override
    @Cacheable("listAllItemBz2Md5")
    public String listAllItemBz2Md5() {
        CaffeineCache itemBz2Cache = (CaffeineCache) cacheManager.getCache("listAllItemBz2");
        byte[] allItemBz2;
        if (itemBz2Cache != null) {
            if (!itemBz2Cache.getNativeCache().asMap().isEmpty()) {
                allItemBz2 = (byte[]) itemBz2Cache.getNativeCache().getIfPresent("allItemBz2");
                if (allItemBz2 == null) {
                    itemBz2Cache.evict("allItemBz2");
                    allItemBz2 = listAllItemBz2();
                }
            } else {
                itemBz2Cache.evict("allItemBz2");
                allItemBz2 = listAllItemBz2();
            }
        } else {
            allItemBz2 = listAllItemBz2();
        }
        return DigestUtils.md5DigestAsHex(allItemBz2);
    }
}
