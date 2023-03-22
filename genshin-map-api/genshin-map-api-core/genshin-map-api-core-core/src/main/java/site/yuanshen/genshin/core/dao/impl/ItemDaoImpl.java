package site.yuanshen.genshin.core.dao.impl;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;
import site.yuanshen.common.core.utils.CompressUtils;
import site.yuanshen.common.core.utils.PgsqlUtils;
import site.yuanshen.data.dto.ItemDto;
import site.yuanshen.data.entity.Item;
import site.yuanshen.data.entity.ItemTypeLink;
import site.yuanshen.data.entity.Marker;
import site.yuanshen.data.entity.MarkerItemLink;
import site.yuanshen.data.mapper.*;
import site.yuanshen.data.vo.ItemVo;
import site.yuanshen.genshin.core.dao.ItemDao;

import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

/**
 * TODO
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
//        List<ItemTypeLink> typeLinkList = itemTypeLinkMapper.selectList(Wrappers.<ItemTypeLink>lambdaQuery()
//                .in(ItemTypeLink::getItemId,
//                        itemList.stream()
//                                .map(Item::getId).distinct().collect(Collectors.toList())));
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
//        List<MarkerItemLink> markerItemLinkList = markerItemLinkMapper.selectList(Wrappers.<MarkerItemLink>lambdaQuery()
//                .in(MarkerItemLink::getItemId,
//                        itemList.stream()
//                                .map(Item::getId).distinct().collect(Collectors.toList())));

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
                .map(itemDto -> itemDto.setCount(Optional.ofNullable(markerItemLinkCount.get(itemDto.getItemId())).orElse(0)))
                .map(itemDto -> itemDto.setTypeIdList(itemToTypeMap.get(itemDto.getItemId())))
                .map(ItemDto::getVo)
                .sorted(Comparator.comparing(ItemVo::getSortIndex).reversed()).collect(Collectors.toList());
    }

    /**
     * @return 所有的物品信息的Bz2压缩
     */
    @Override
    @Cacheable(value = "listAllItemBz2")
    public byte[] listAllItemBz2() {
        throw new RuntimeException("缓存未创建");
    }

    /**
     * 刷新物品压缩缓存并返回压缩文档
     *
     * @return 物品压缩文档
     */
    @Override
    public String refreshAllItemBz2() {
        try {
            List<ItemVo> itemList = listAllItem();
            itemList.sort(Comparator.comparingLong(ItemVo::getItemId));
            Cache bz2Cache = cacheManager.getCache("listAllItemBz2Md5");
            if (bz2Cache == null) throw new RuntimeException("缓存未初始化");
            String result = JSON.toJSONString(itemList);
            bz2Cache.put(0, result.getBytes(StandardCharsets.UTF_8));
            return result;
        } catch (Exception e) {
            throw new RuntimeException("创建压缩失败",e);
        }
    }
}
