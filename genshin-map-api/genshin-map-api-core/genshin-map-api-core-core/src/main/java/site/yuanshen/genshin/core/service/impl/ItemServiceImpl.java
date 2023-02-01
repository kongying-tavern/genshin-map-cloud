package site.yuanshen.genshin.core.service.impl;

import cn.hutool.core.util.ObjUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import site.yuanshen.data.dto.ItemDto;
import site.yuanshen.data.dto.ItemSearchDto;
import site.yuanshen.data.entity.*;
import site.yuanshen.data.mapper.*;
import site.yuanshen.data.vo.ItemVo;
import site.yuanshen.data.vo.helper.PageListVo;
import site.yuanshen.genshin.core.convert.HistoryConvert;
import site.yuanshen.genshin.core.service.ItemService;
import site.yuanshen.genshin.core.service.mbp.ItemMBPService;
import site.yuanshen.genshin.core.service.mbp.ItemTypeLinkMBPService;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * 物品服务接口实现
 *
 * @author Moment
 */
@Service
@RequiredArgsConstructor
public class ItemServiceImpl implements ItemService {

    private final ItemMapper itemMapper;
    private final ItemMBPService itemMBPService;
    private final ItemTypeMapper itemTypeMapper;
    private final ItemTypeLinkMapper itemTypeLinkMapper;
    private final ItemTypeLinkMBPService itemTypeLinkMBPService;
    private final MarkerItemLinkMapper markerItemLinkMapper;
    private final ItemAreaPublicMapper itemAreaPublicMapper;
    private final MarkerMapper markerMapper;

    private final HistoryMapper historyMapper;

    /**
     * 根据物品ID查询物品
     *
     * @param itemIdList 物品ID列表
     * @return 物品数据封装列表
     */
    @Override
    @Cacheable("listItemById")
    public List<ItemDto> listItemById(List<Long> itemIdList, List<Integer> hiddenFlagList) {
        //收集分类信息
        Map<Long, List<Long>> typeMap = new ConcurrentHashMap<>();
        itemTypeLinkMapper.selectList(Wrappers.<ItemTypeLink>lambdaQuery()
                        .in(ItemTypeLink::getItemId, itemIdList))
                .parallelStream()
                .forEach(typeLink ->
                        typeMap.compute(typeLink.getItemId(), (itemId, typeList) -> {
                            if (typeList == null) return new ArrayList<>(Collections.singletonList(typeLink.getTypeId()));
                            typeList.add(typeLink.getTypeId());
                            return typeList;
                        }));
        //取得实体类并转化为DTO，过程之中写入分类信息
        return itemMapper.selectList(Wrappers.<Item>lambdaQuery()
                        .in(!hiddenFlagList.isEmpty(), Item::getHiddenFlag, hiddenFlagList)
                        .in(Item::getId, itemIdList))
                .parallelStream()
                .map(item ->
                        new ItemDto(item)
                                .setTypeIdList(typeMap.getOrDefault(item.getId(), new ArrayList<>())))
                .sorted(Comparator.comparing(ItemDto::getSortIndex).reversed())
                .collect(Collectors.toList());
    }

    /**
     * 根据筛选条件列出物品信息
     *
     * @param itemSearchDto @return 物品ID列表
     */
    @Override
    @Cacheable("listItem")
    public PageListVo<ItemVo> listItem(ItemSearchDto itemSearchDto) {
//        itemSearchDto.setIsTestUser(Boolean.TRUE);
        Page<Item> itemPage = itemMapper.selectPageItem(itemSearchDto.getPageEntity(), itemSearchDto);
        itemPage.setRecords(itemPage.getRecords().parallelStream().distinct().collect(Collectors.toList()));
        if (itemPage.getTotal() == 0L)
            return new PageListVo<ItemVo>().setRecord(new ArrayList<>()).setSize(itemPage.getSize()).setTotal(0L);
        //获取分类数据
        List<ItemTypeLink> typeLinkList = itemTypeLinkMapper.selectList(Wrappers.<ItemTypeLink>lambdaQuery()
                .in(ItemTypeLink::getItemId,
                        itemPage.getRecords().stream()
                                .map(Item::getId).collect(Collectors.toList())));
        Map<Long, List<Long>> itemToTypeMap = new HashMap<>();
        for (ItemTypeLink typeLink : typeLinkList) {
            Long itemId = typeLink.getItemId();
            List<Long> typeList = itemToTypeMap.getOrDefault(itemId, new ArrayList<>());
            typeList.add(typeLink.getTypeId());
            itemToTypeMap.put(itemId, typeList);
        }



        //先过滤出正常点位
        //计算各个物品在点位中的数量合计
        Map<Long, Integer> markerItemLinkCount = new HashMap<>();

        //获取点位数据
        List<MarkerItemLink> markerItemLinkList = markerItemLinkMapper.selectList(Wrappers.<MarkerItemLink>lambdaQuery()
                .in(MarkerItemLink::getItemId,
                        itemPage.getRecords().stream()
                                .map(Item::getId).collect(Collectors.toList())));

        //获取其中的正常点位 hidden_flag=0 若为内鬼用户,则增加hidden_flag=2
        List<Long> normalMarkerList;

        if (!markerItemLinkList.isEmpty()) {
            normalMarkerList = markerMapper.selectList(Wrappers.<Marker>lambdaQuery()
                            .in(!itemSearchDto.getHiddenFlagList().isEmpty(),Marker::getHiddenFlag,itemSearchDto.getHiddenFlagList())
                            .in(Marker::getId, markerItemLinkList.stream().map(MarkerItemLink::getMarkerId).collect(Collectors.toList())))
                    .stream().map(Marker::getId).collect(Collectors.toList());
            markerItemLinkList.stream().filter(markerItemLink -> normalMarkerList.contains(markerItemLink.getMarkerId()))
                    .collect(Collectors.groupingBy(MarkerItemLink::getItemId)).forEach(
                            (itemId, list) -> markerItemLinkCount.put(itemId, list.stream().mapToInt(MarkerItemLink::getCount).sum())
                    );
        }


        return new PageListVo<ItemVo>()
                .setRecord(itemPage.getRecords().stream()
                        .map(ItemDto::new)
                        .map(itemDto -> itemDto.setCount(Optional.ofNullable(markerItemLinkCount.get(itemDto.getItemId())).orElse(0)))
                        .map(itemDto -> itemDto.setTypeIdList(itemToTypeMap.get(itemDto.getItemId())))
                        .map(ItemDto::getVo)
                        .sorted(Comparator.comparing(ItemVo::getSortIndex).thenComparing(ItemVo::getItemId).reversed()).collect(Collectors.toList()))
                .setTotal(itemPage.getTotal())
                .setSize(itemPage.getSize());
    }

    /**
     * 修改物品
     *
     * @param itemVoList 物品前端封装
     * @param editSame   是否编辑同名的所有物品，1为是，0为否
     * @return 是否成功
     */
    @Override
    @Transactional
    public Boolean updateItem(List<ItemVo> itemVoList, Integer editSame) {
        for (ItemVo itemVo : itemVoList) {
            ItemDto itemDto = new ItemDto(itemVo);
            //同名编辑
            List<Item> sameItems = new ArrayList<>();
            if (editSame.equals(1)) {
                sameItems = itemMapper.selectList(Wrappers.<Item>lambdaQuery()
                        .eq(Item::getName, itemDto.getName()));
            }
            //物品ID
            List<Long> itemIds = new ArrayList<>(Collections.singletonList(itemDto.getItemId()));
            itemIds.addAll(sameItems.parallelStream().map(Item::getId).collect(Collectors.toList()));

            //在更新逻辑之前做历史信息记录
            saveHistoryItem(itemIds, sameItems);


            //对比类型信息是否更改
            Set<Long> oldTypeIds = itemTypeLinkMapper.selectList(Wrappers.<ItemTypeLink>lambdaQuery()
                            .eq(ItemTypeLink::getItemId, itemDto.getItemId()))
                    .stream()
                    .map(ItemTypeLink::getTypeId).collect(Collectors.toSet());
            Set<Long> newTypeIds = new TreeSet<>(itemDto.getTypeIdList());
            //如果更改了就进行分类的刷新
            if (!oldTypeIds.equals(newTypeIds)) {
                //删除旧分类连接
                itemTypeLinkMapper.delete(Wrappers.<ItemTypeLink>lambdaQuery()
                        .in(ItemTypeLink::getItemId, itemIds));
                //非同名
                if (!editSame.equals(1)) {
                    itemTypeLinkMBPService.saveBatch(
                            newTypeIds.stream()
                                    .map(id -> new ItemTypeLink()
                                            .setItemId(itemDto.getItemId())
                                            .setTypeId(id))
                                    .collect(Collectors.toList())
                    );
                }
                //同名
                else {
                    List<ItemTypeLink> newLink = new ArrayList<>();
                    itemIds.parallelStream().forEach(itemId -> {
                        newLink.addAll(newTypeIds.stream()
                                .map(id -> new ItemTypeLink()
                                        .setItemId(itemId)
                                        .setTypeId(id))
                                .collect(Collectors.toList()));
                    });
                    itemTypeLinkMBPService.saveBatch(newLink);
                }
            }
            itemMapper.update(null,
                    Wrappers.<Item>lambdaUpdate().in(Item::getId, itemIds)
                            .set(itemDto.getName() != null, Item::getName, itemDto.getName())
                            .set(itemDto.getDefaultContent() != null, Item::getDefaultContent, itemDto.getDefaultContent())
                            .set(itemDto.getIconTag() != null, Item::getIconTag, itemDto.getIconTag())
                            .set(itemDto.getIconStyleType() != null, Item::getIconStyleType, itemDto.getIconStyleType())
                            .set(itemDto.getHiddenFlag() != null, Item::getHiddenFlag, itemDto.getHiddenFlag())
                            .set(itemDto.getDefaultRefreshTime() != null, Item::getDefaultRefreshTime, itemDto.getDefaultRefreshTime())
                            .set(itemDto.getSortIndex() != null, Item::getSortIndex, itemDto.getSortIndex())
            );
        }
        return true;
    }


    /**
     * 将物品加入某一类型
     *
     * @param itemIdList 物品ID列表
     * @param typeId     类型ID
     * @return 是否成功
     */
    @Override
    @Transactional
    public Boolean joinItemsInType(List<Long> itemIdList, Long typeId) {
        if (itemTypeMapper.selectOne(Wrappers.<ItemType>lambdaQuery().eq(ItemType::getId, typeId)) == null)
            throw new RuntimeException("类型ID错误");
        if (!itemMapper.selectCount(Wrappers.<Item>lambdaQuery().in(Item::getId, itemIdList)).equals((long) itemIdList.size()))
            throw new RuntimeException("物品ID存在错误");
        boolean res = itemTypeLinkMBPService.saveBatch(
                itemIdList.stream()
                        .map(id -> new ItemTypeLink()
                                .setItemId(id)
                                .setTypeId(typeId))
                        .collect(Collectors.toList())
        );
        return res;
    }

    /**
     * 新增物品
     *
     * @param itemDto 物品数据封装
     * @return 新物品ID
     */
    @Override
    @Transactional
    public Long createItem(ItemDto itemDto) {
        Item item = itemDto.getEntity();
        itemMapper.insert(item);
        //处理类型信息
        List<Long> typeIdList = itemDto.getTypeIdList();
        if (typeIdList != null) {
            //判断是否有不存在的类型ID
            if (typeIdList.size() != itemTypeMapper.selectCount(Wrappers.<ItemType>lambdaQuery().in(ItemType::getId, typeIdList)))
                throw new RuntimeException("类型ID错误");
            itemTypeLinkMBPService.saveBatch(
                    typeIdList.stream()
                            .map(id -> new ItemTypeLink().setItemId(item.getId()).setTypeId(id))
                            .collect(Collectors.toList())
            );
        }
        return item.getId();
    }

    /**
     * 复制物品到地区
     *
     * @param itemIdList 物品ID列表
     * @param areaId     地区ID
     * @return 物品复制到地区结果前端封装
     */
    @Override
    @Transactional
    public List<Long> copyItemToArea(List<Long> itemIdList, Long areaId) {
        //TODO 判断是否是末端地区，检查所有涉及地区的代码
        List<Item> items = itemMapper.selectList(Wrappers.<Item>lambdaQuery()
                .in(Item::getId, itemIdList));
        for (Item item : items) {
            //先根据id找到typeLink中的数据
            List<ItemTypeLink> itemTypeLinks = itemTypeLinkMapper.selectList(Wrappers.<ItemTypeLink>lambdaQuery().eq(ItemTypeLink::getItemId, item.getId()));

            item.setId(null);
            item.setAreaId(areaId);

            //新增记录
            itemMBPService.save(item);
            //根据新id给typeLink赋值并重新插入一份
            itemTypeLinks = itemTypeLinks.stream().map(itemTypeLink ->
                    itemTypeLink.setItemId(item.getId()).setId(null)
            ).collect(Collectors.toList());
            itemTypeLinkMBPService.saveBatch(itemTypeLinks);
        }

        return items.parallelStream().map(Item::getId).collect(Collectors.toList());
    }

    /**
     * 删除物品
     *
     * @param itemId 物品ID
     * @return 是否成功
     */
    @Override
    @Transactional
    public Boolean deleteItem(Long itemId) {
        //增加公共物品拦截逻辑
        ItemAreaPublic itemAreaPublic = itemAreaPublicMapper.selectOne(Wrappers.<ItemAreaPublic>lambdaQuery()
                .eq(ItemAreaPublic::getItemId, itemId));
        if (ObjUtil.isNotNull(itemAreaPublic)){
            throw new RuntimeException("不允许删除公共物品");
        }


        itemTypeLinkMapper.delete(Wrappers.<ItemTypeLink>lambdaQuery()
                .eq(ItemTypeLink::getItemId, itemId));
        markerItemLinkMapper.delete(Wrappers.<MarkerItemLink>lambdaQuery()
                .eq(MarkerItemLink::getItemId, itemId));
        boolean deleted = itemMapper.delete(Wrappers.<Item>lambdaQuery()
                .eq(Item::getId, itemId)) == 1;
        return deleted;
    }

    //---------------存储历史信息-------------------

    /**
     * @param itemIds
     * @param sameItems 同名物品List(若无需同名则默认为空)
     */
    private void saveHistoryItem(List<Long> itemIds, List<Item> sameItems) {
        //根据itemId查询(按目前逻辑只有一个Id),加上同名物品
        List<Item> items = itemMapper.selectList(Wrappers.<Item>lambdaQuery()
                .in(Item::getId, itemIds));
        items.addAll(sameItems);

        //根据每一个物品查询出对应的link记录,并转为DTO
        List<ItemDto> itemDtoList = items.stream().map(
                item -> {
                    List<Long> typeLink = itemTypeLinkMapper.selectList(Wrappers.<ItemTypeLink>lambdaQuery()
                                    .eq(ItemTypeLink::getItemId, item.getId()))
                            .stream().map(ItemTypeLink::getTypeId).collect(Collectors.toList());

                    return new ItemDto(item).setTypeIdList(typeLink);
                }
        ).collect(Collectors.toList());

        //将DTO转为history
        itemDtoList.forEach(
                dto -> {
                    History history = HistoryConvert.convert(dto);
                    //存储入库
                    historyMapper.insert(history);
                }
        );
    }

}
