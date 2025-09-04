package site.yuanshen.genshin.core.service;

import cn.hutool.core.util.ObjUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import site.yuanshen.common.core.exception.GenshinApiException;
import site.yuanshen.common.core.utils.PgsqlUtils;
import site.yuanshen.data.dto.ItemDto;
import site.yuanshen.data.dto.ItemSearchDto;
import site.yuanshen.data.entity.*;
import site.yuanshen.data.enums.HistoryEditType;
import site.yuanshen.data.mapper.*;
import site.yuanshen.data.vo.ItemVo;
import site.yuanshen.data.vo.helper.PageListVo;
import site.yuanshen.genshin.core.convert.HistoryConvert;
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
public class ItemService {

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
        List<ItemDto> result = itemMapper.selectList(Wrappers.<Item>lambdaQuery()
                        .in(!hiddenFlagList.isEmpty(), Item::getHiddenFlag, hiddenFlagList)
                        .in(Item::getId, itemIdList))
                .parallelStream()
                .map(item ->
                        new ItemDto(item)
                                .withTypeIdList(typeMap.getOrDefault(item.getId(), new ArrayList<>())))
                .sorted(Comparator.comparing(ItemDto::getSortIndex).reversed())
                .collect(Collectors.toList());
        return result;
    }

    /**
     * 根据筛选条件列出物品信息
     *
     * @param itemSearchDto @return 物品ID列表
     */
    @Cacheable("listItem")
    public PageListVo<ItemVo> listItem(ItemSearchDto itemSearchDto) {
        QueryWrapper<Item> wrapper = Wrappers.<Item>query();
        // 处理排序
        final List<PgsqlUtils.Sort<Item>> sortList = PgsqlUtils.toSortConfigurations(
            itemSearchDto.getSort(),
            PgsqlUtils.SortConfig.<Item>create()
                .addEntry(PgsqlUtils.SortConfigItem.<Item>create().withProp("sortIndex"))
        );
        wrapper = PgsqlUtils.sortWrapper(wrapper, sortList);

        Page<Item> itemPage = itemMapper.selectPageItem(itemSearchDto.getPageEntity(), itemSearchDto, wrapper.lambda());
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

        List<ItemVo> result = itemPage.getRecords().stream()
                .map(ItemDto::new)
                .map(dto -> dto
                    .withCount(markerItemLinkCount.getOrDefault(dto.getId(), 0))
                    .withTypeIdList((itemToTypeMap.get(dto.getId())))
                )
                .map(ItemDto::getVo)
                .collect(Collectors.toList());
        return new PageListVo<ItemVo>()
                .setRecord(result)
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
    @Transactional
    public Boolean updateItem(List<ItemVo> itemVoList, Integer editSame) {
        for (ItemVo itemVo : itemVoList) {
            ItemDto itemDto = new ItemDto(itemVo);
            List<Long> typeIdList = itemVo.getTypeIdList();
            //同名编辑
            List<Item> sameItems = new ArrayList<>();
            if (editSame.equals(1)) {
                sameItems = itemMapper.selectList(Wrappers.<Item>lambdaQuery()
                        .eq(Item::getName, itemDto.getName()));
            }
            //物品ID
            List<Long> itemIds = new ArrayList<>(Collections.singletonList(itemDto.getId()));
            itemIds.addAll(sameItems.parallelStream().map(Item::getId).collect(Collectors.toList()));

            //在更新逻辑之前做历史信息记录
            saveHistoryItem(itemIds, sameItems, HistoryEditType.UPDATE);

            //对比类型信息是否更改
            HashSet<Long> oldTypeIds = itemTypeLinkMapper.selectList(Wrappers.<ItemTypeLink>lambdaQuery()
                            .eq(ItemTypeLink::getItemId, itemDto.getId()))
                    .stream()
                    .map(ItemTypeLink::getTypeId).collect(Collectors.toCollection(HashSet::new));
            HashSet<Long> newTypeIds = new HashSet<>(typeIdList);
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
                                            .withItemId(itemDto.getId())
                                            .withTypeId(id))
                                    .collect(Collectors.toList())
                    );
                }
                //同名
                else {
                    List<ItemTypeLink> newLink = new ArrayList<>();
                    itemIds.parallelStream().forEach(itemId -> {
                        newLink.addAll(newTypeIds.stream()
                                .map(id -> new ItemTypeLink()
                                        .withItemId(itemId)
                                        .withTypeId(id))
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
                            .set(itemDto.getSpecialFlag() != null, Item::getSpecialFlag, itemDto.getSpecialFlag())
            );
            if(itemDto.getAreaId() != null) {
                itemMapper.update(null,
                        Wrappers.<Item>lambdaUpdate().eq(Item::getId, itemDto.getId())
                                .set(itemDto.getAreaId() != null, Item::getAreaId, itemDto.getAreaId())
                );
            }
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
    @Transactional
    public Boolean joinItemsInType(List<Long> itemIdList, Long typeId) {
        if (itemTypeMapper.selectOne(Wrappers.<ItemType>lambdaQuery().eq(ItemType::getId, typeId)) == null)
            throw new GenshinApiException("类型ID错误");
        if (!itemMapper.selectCount(Wrappers.<Item>lambdaQuery().in(Item::getId, itemIdList)).equals((long) itemIdList.size()))
            throw new GenshinApiException("物品ID存在错误");
        boolean res = itemTypeLinkMBPService.saveBatch(
                itemIdList.stream()
                        .map(id -> new ItemTypeLink()
                                .withItemId(id)
                                .withTypeId(typeId))
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
    @Transactional
    public Long createItem(ItemDto itemDto) {
        Item item = itemDto.getEntity();
        itemMapper.insert(item);
        //处理类型信息
        List<Long> typeIdList = itemDto.getTypeIdList();
        if (typeIdList != null) {
            //判断是否有不存在的类型ID
            if (typeIdList.size() != itemTypeMapper.selectCount(Wrappers.<ItemType>lambdaQuery().in(ItemType::getId, typeIdList)))
                throw new GenshinApiException("类型ID错误");
            //批量保存
            itemTypeLinkMBPService.saveBatch(
                    typeIdList.stream()
                            .map(id -> new ItemTypeLink().withItemId(item.getId()).withTypeId(id))
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
                    itemTypeLink.withItemId(item.getId()).withId(null)
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
    @Transactional
    public Boolean deleteItem(Long itemId) {
        //增加公共物品拦截逻辑
        ItemAreaPublic itemAreaPublic = itemAreaPublicMapper.selectOne(Wrappers.<ItemAreaPublic>lambdaQuery()
                .eq(ItemAreaPublic::getItemId, itemId));
        if (ObjUtil.isNotNull(itemAreaPublic)){
            throw new GenshinApiException("不允许删除公共物品");
        }

        // 添加删除物品历史记录
        saveHistoryItem(List.of(itemId), List.of(), HistoryEditType.DELETE);

        itemTypeLinkMapper.delete(Wrappers.<ItemTypeLink>lambdaQuery()
                .eq(ItemTypeLink::getItemId, itemId));
        markerItemLinkMapper.delete(Wrappers.<MarkerItemLink>lambdaQuery()
                .eq(MarkerItemLink::getItemId, itemId));
        return itemMapper.delete(Wrappers.<Item>lambdaQuery()
                .eq(Item::getId, itemId)) == 1;
    }

    //---------------存储历史信息-------------------

    /**
     * 将当前点位信息存入历史记录表
     * @param itemIds 物品Id列表
     * @param sameItems 同名物品List(若无需同名则默认为空)
     * @param editType 编辑类型
     */
    private void saveHistoryItem(List<Long> itemIds, List<Item> sameItems, HistoryEditType editType) {
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

                    return new ItemDto(item).withTypeIdList(typeLink);
                }
        ).collect(Collectors.toList());

        //将DTO转为history
        itemDtoList.forEach(
                dto -> {
                    History history = HistoryConvert.convert(dto, editType);
                    //存储入库
                    historyMapper.insert(history);
                }
        );
    }

}
