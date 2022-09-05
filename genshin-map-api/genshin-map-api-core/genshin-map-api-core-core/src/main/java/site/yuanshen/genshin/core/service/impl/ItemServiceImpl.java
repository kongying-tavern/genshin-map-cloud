package site.yuanshen.genshin.core.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import site.yuanshen.data.dto.ItemDto;
import site.yuanshen.data.dto.ItemSearchDto;
import site.yuanshen.data.dto.ItemTypeDto;
import site.yuanshen.data.dto.helper.PageAndTypeListDto;
import site.yuanshen.data.dto.helper.PageSearchDto;
import site.yuanshen.data.entity.*;
import site.yuanshen.data.mapper.*;
import site.yuanshen.data.vo.ItemTypeVo;
import site.yuanshen.data.vo.ItemVo;
import site.yuanshen.data.vo.helper.PageListVo;
import site.yuanshen.genshin.core.convert.HistoryConvert;
import site.yuanshen.genshin.core.service.CacheService;
import site.yuanshen.genshin.core.service.ItemService;
import site.yuanshen.genshin.core.service.mbp.ItemAreaPublicMBPService;
import site.yuanshen.genshin.core.service.mbp.ItemMBPService;
import site.yuanshen.genshin.core.service.mbp.ItemTypeLinkMBPService;
import site.yuanshen.genshin.core.service.mbp.ItemTypeMBPService;

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

    private final CacheService cacheService;
    private final ItemMapper itemMapper;
    private final ItemMBPService itemMBPService;
    private final ItemTypeMapper itemTypeMapper;
    private final ItemTypeMBPService itemTypeMBPService;
    private final ItemTypeLinkMapper itemTypeLinkMapper;
    private final ItemTypeLinkMBPService itemTypeLinkMBPService;
    private final ItemAreaPublicMapper itemAreaPublicMapper;
    private final ItemAreaPublicMBPService itemAreaPublicMBPService;
    private final MarkerItemLinkMapper markerItemLinkMapper;


    private final HistoryMapper historyMapper;

    /**
     * 列出物品类型
     *
     * @param searchDto 带分类的分页查询数据封装
     * @param self      查询自身还是查询子级，0为查询自身，1为查询子级
     * @return 物品类型的前端封装的分页封装
     */
    @Override
    @Cacheable("listItemType")
    public PageListVo<ItemTypeVo> listItemType(PageAndTypeListDto searchDto, Integer self,Boolean isTestUser) {
        Page<ItemType> itemTypePage = new Page<>();
        //查询自身
        List<Long> typeIdList = searchDto.getTypeIdList();
        if (self.equals(0)) {
            if (typeIdList != null && typeIdList.size() > 0) {
                itemTypePage = itemTypeMapper.selectPage(searchDto.getPageEntity(), Wrappers.<ItemType>lambdaQuery()
                        .ne(!isTestUser,ItemType::getHiddenFlag,2)
                        .in(ItemType::getId, typeIdList));
            } else {
                itemTypePage.setTotal(0L);
            }
        }
        //查询子级
        else if (self.equals(1)) {
            itemTypePage = itemTypeMapper.selectPage(searchDto.getPageEntity(), Wrappers.<ItemType>lambdaQuery()
                    .ne(!isTestUser,ItemType::getHiddenFlag,2)
                    .in(ItemType::getParentId,
                            typeIdList != null && typeIdList.size() > 0 ? typeIdList : Collections.singletonList(-1L)));
        }
        return new PageListVo<ItemTypeVo>()
                .setRecord(itemTypePage.getRecords().stream()
                        .map(ItemTypeDto::new).map(ItemTypeDto::getVo)
                        .sorted(Comparator.comparing(ItemTypeVo::getSortIndex).reversed())
                        .collect(Collectors.toList()))
                .setSize(itemTypePage.getSize())
                .setTotal(itemTypePage.getTotal());
    }

    /**
     * 添加物品类型
     *
     * @param itemTypeDto 物品类型数据封装
     * @return 新物品类型ID
     */
    @Override
    public Long addItemType(ItemTypeDto itemTypeDto) {
        ItemType itemType = itemTypeDto.getEntity();
        //临时id
//				.setTypeId(-1L);
        itemTypeMapper.insert(itemType);
        //正式更新id
//		itemTypeMapper.updateById(itemType.setTypeId(itemType.getId()));
        //设置父级
        if (!itemType.getParentId().equals(-1L)) {
            itemTypeMapper.update(null, Wrappers.<ItemType>lambdaUpdate()
                    .eq(ItemType::getId, itemType.getParentId())
                    .set(ItemType::getIsFinal, false));
        }
        cacheService.cleanItemCache();
        return itemType.getId();
    }

    /**
     * 修改物品类型
     *
     * @param itemTypeDto 物品类型数据封装
     * @return 是否成功
     */
    @Override
    public Boolean updateItemType(ItemTypeDto itemTypeDto) {
        //获取类型实体
        ItemType itemType = itemTypeMapper.selectOne(Wrappers.<ItemType>lambdaQuery()
                .eq(ItemType::getId, itemTypeDto.getTypeId()));
        //设置内容 TODO 检查所有的set是否包含了所有的属性 或者直接替换成属性复制工具
        itemType.setIconTag(itemTypeDto.getIconTag());
        itemType.setName(itemTypeDto.getName());
        itemType.setContent(itemTypeDto.getContent());
        //判断是否是末端类型
        itemType.setIsFinal(
                itemTypeMapper.selectCount(Wrappers.<ItemType>lambdaQuery()
                        .eq(ItemType::getParentId, itemType.getId()))
                        > 0);
        //更改分类类型末端标志
        if (!itemType.getParentId().equals(itemTypeDto.getParentId())) {
            itemTypeMapper.update(null, Wrappers.<ItemType>lambdaUpdate()
                    .eq(ItemType::getId, itemTypeDto.getParentId())
                    .set(ItemType::getIsFinal, false));
            //更改原父级的末端标志(如果原父级只剩这个子级的话)
            if (itemTypeMapper.selectCount(Wrappers.<ItemType>lambdaQuery()
                    .eq(ItemType::getParentId, itemType.getParentId()))
                    > 0) {
                itemTypeMapper.update(null, Wrappers.<ItemType>lambdaUpdate()
                        .eq(ItemType::getId, itemType.getParentId())
                        .set(ItemType::getIsFinal, true));
            }
            itemType.setParentId(itemTypeDto.getParentId());
        }
        //更新实体
        itemTypeMapper.updateById(itemType);
        cacheService.cleanItemCache();
        return true;
    }

    /**
     * 批量移动类型为目标类型的子类型
     *
     * @param itemTypeIdList 类型ID列表
     * @param targetTypeId   目标类型ID
     * @return 是否成功
     */
    @Override
    public Boolean moveItemType(List<Long> itemTypeIdList, Long targetTypeId) {
        //选取实体
        List<ItemType> itemTypeList = itemTypeMapper.selectList(Wrappers.<ItemType>lambdaQuery()
                .in(ItemType::getId, itemTypeIdList));
        //读取父级
        List<Long> parentIdList = itemTypeList.parallelStream().map(ItemType::getParentId).distinct().collect(Collectors.toList());
        //更改父级
        itemTypeList = itemTypeList.parallelStream().peek(itemType -> itemType.setParentId(targetTypeId)).collect(Collectors.toList());
        itemTypeMapper.update(null, Wrappers.<ItemType>lambdaUpdate()
                .eq(ItemType::getId, targetTypeId)
                .set(ItemType::getIsFinal, false));
        //更新实体
        itemTypeMBPService.updateBatchById(itemTypeList);
        //更新原父级
        parentIdList.parallelStream().forEach(parentId -> {
            if (itemTypeMapper.selectCount(Wrappers.<ItemType>lambdaQuery()
                    .eq(ItemType::getParentId, parentId)) == 0) {
                itemTypeMapper.update(null, Wrappers.<ItemType>lambdaUpdate()
                        .eq(ItemType::getId, parentId)
                        .set(ItemType::getIsFinal, true));
            }
        });
        cacheService.cleanItemCache();
        return true;
    }

    /**
     * 批量递归删除物品类型
     *
     * @param itemTypeId 类型ID列表
     * @return 是否成功
     */
    @Override
    public Boolean deleteItemType(Long itemTypeId) {
        List<Long> nowTypeIdList = Collections.singletonList(itemTypeId);
        while (!nowTypeIdList.isEmpty()) {
            //删除类型信息
            itemTypeMapper.delete(Wrappers.<ItemType>lambdaQuery().in(ItemType::getId, nowTypeIdList));
            //删除类型关联
            itemTypeLinkMapper.delete(Wrappers.<ItemTypeLink>lambdaQuery().in(ItemTypeLink::getTypeId, nowTypeIdList));
            //查找所有子级
            nowTypeIdList = itemTypeMapper.selectList(Wrappers.<ItemType>lambdaQuery().in(ItemType::getParentId, nowTypeIdList))
                    .parallelStream()
                    .map(ItemType::getId).distinct().collect(Collectors.toList());
        }
        cacheService.cleanItemCache();
        return true;
    }

    /**
     * 根据物品ID查询物品
     *
     * @param itemIdList 物品ID列表
     * @return 物品数据封装列表
     */
    @Override
    @Cacheable("listItemById")
    public List<ItemDto> listItemById(List<Long> itemIdList,Boolean isTestUser) {
        //收集分类信息
        Map<Long, List<Long>> typeMap = new ConcurrentHashMap<>();
        itemTypeLinkMapper.selectList(Wrappers.<ItemTypeLink>lambdaQuery()
                        .in(ItemTypeLink::getItemId, itemIdList))
                .parallelStream()
                .forEach(typeLink ->
                        typeMap.compute(typeLink.getItemId(), (itemId, typeList) -> {
                            if (typeList == null) return Collections.singletonList(typeLink.getTypeId());
                            typeList.add(typeLink.getTypeId());
                            return typeList;
                        }));
        //取得实体类并转化为DTO，过程之中写入分类信息
        return itemMapper.selectList(Wrappers.<Item>lambdaQuery()
                        .ne(!isTestUser,Item::getHiddenFlag,2)
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
        Page<Item> itemPage = itemMapper.selectPageItem(itemSearchDto.getPageEntity(), itemSearchDto);
        if (itemPage.getTotal() == 0L)
            return new PageListVo<ItemVo>().setRecord(new ArrayList<>()).setSize(itemPage.getSize()).setTotal(0L);
        //获取分类数据
        List<ItemTypeLink> typeLinkList = itemTypeLinkMapper.selectList(Wrappers.<ItemTypeLink>lambdaQuery()
                .in(ItemTypeLink::getItemId,
                        itemPage.getRecords().stream()
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
                        itemPage.getRecords().stream()
                                .map(Item::getId).distinct().collect(Collectors.toList())));
        //计算各个物品在点位中的数量合计
        Map<Long, Integer> markerItemLinkCount = new HashMap<>();
        markerItemLinkList.stream().collect(Collectors.groupingBy(MarkerItemLink::getItemId)).forEach(
                (itemId, list) -> markerItemLinkCount.put(itemId, list.stream().mapToInt(MarkerItemLink::getCount).sum())
        );

        return new PageListVo<ItemVo>()
                .setRecord(itemPage.getRecords().stream()
                        .map(ItemDto::new)
                        .map(itemDto -> itemDto.setCount(Optional.ofNullable(markerItemLinkCount.get(itemDto.getItemId())).orElse(0)))
                        .map(itemDto -> itemDto.setTypeIdList(itemToTypeMap.get(itemDto.getItemId())))
                        .map(ItemDto::getVo)
                        .sorted(Comparator.comparing(ItemVo::getSortIndex).reversed()).collect(Collectors.toList()))
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
            saveHistoryItem(itemIds,sameItems);


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
        cacheService.cleanItemCache();
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
        cacheService.cleanItemCache();
        return res;
    }

    /**
     * 新增物品
     *
     * @param itemDto 物品数据封装
     * @return 新物品ID
     */
    @Override
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
        cacheService.cleanItemCache();
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
    public List<Long> copyItemToArea(List<Long> itemIdList, Long areaId) {
        //TODO 判断是否是末端地区，检查所有涉及地区的代码
        //TODO ID冲突问题
        List<Item> items = itemMapper.selectList(Wrappers.<Item>lambdaQuery()
                .in(Item::getId, itemIdList));
//		long id = itemMapper.selectOne(Wrappers.<Item>query().select("max(item_id) as itemId"))
//				.getId() + 1;
        for (Item item : items) {

            //先根据id找到typeLink中的数据
            List<ItemTypeLink> itemTypeLinks = itemTypeLinkMapper.selectList(Wrappers.<ItemTypeLink>lambdaQuery().eq(ItemTypeLink::getItemId, item.getId()));

            //2022.07.07 清空主键id
            item.setId(null);
//			item.setItemId(id);
            item.setAreaId(areaId);
//			id++;

            //新增记录
            itemMBPService.save(item);
            //根据新id给typeLink赋值并重新插入一份
            itemTypeLinks = itemTypeLinks.stream().map(itemTypeLink ->
                    itemTypeLink.setItemId(item.getId()).setId(null)
            ).collect(Collectors.toList());
            itemTypeLinkMBPService.saveBatch(itemTypeLinks);

        }
//        itemMBPService.saveBatch(items);
        cacheService.cleanItemCache();

        return items.parallelStream().map(Item::getId).collect(Collectors.toList());
    }

    /**
     * 删除物品
     *
     * @param itemId 物品ID
     * @return 是否成功
     */
    @Override
    public Boolean deleteItem(Long itemId) {
        itemTypeLinkMapper.delete(Wrappers.<ItemTypeLink>lambdaQuery()
                .eq(ItemTypeLink::getItemId, itemId));
        boolean deleted = itemMapper.delete(Wrappers.<Item>lambdaQuery()
                .eq(Item::getId, itemId)) == 1;
        cacheService.cleanItemCache();
        return deleted;
    }

    /**
     * 列出地区公用物品
     *
     * @param pageSearchDto 分页查询数据封装
     * @return 物品前端封装的分页封装
     */
    @Override
    @Cacheable("listCommonItem")
    public PageListVo<ItemVo> listCommonItem(PageSearchDto pageSearchDto) {
        Page<ItemAreaPublic> areaPublicPage = itemAreaPublicMapper.selectPage(pageSearchDto.getPageEntity(), Wrappers.<ItemAreaPublic>query());
        if (areaPublicPage.getTotal() == 0L) {
            return new PageListVo<>(new ArrayList<>(), areaPublicPage.getTotal(), areaPublicPage.getSize());
        }
        return new PageListVo<ItemVo>()
                .setRecord(itemMapper.selectList(Wrappers.<Item>lambdaQuery()
                                .in(Item::getId,
                                        areaPublicPage.getRecords().parallelStream()
                                                .map(ItemAreaPublic::getItemId).collect(Collectors.toList())))
                        .parallelStream().map(ItemDto::new).map(ItemDto::getVo)
                        .sorted(Comparator.comparing(ItemVo::getSortIndex).reversed()).collect(Collectors.toList()))
                .setTotal(areaPublicPage.getTotal())
                .setSize(areaPublicPage.getSize());
    }

    /**
     * 新增地区公用物品
     *
     * @param itemIdList 物品ID列表
     * @return 是否成功
     */
    @Override
    @CacheEvict(value = "listCommonItem",allEntries = true)
    public Boolean addCommonItem(List<Long> itemIdList) {
        return itemAreaPublicMBPService.saveBatch(itemIdList.parallelStream()
                .map(id -> new ItemAreaPublic()
                        .setItemId(id))
                .collect(Collectors.toList()));
    }

    /**
     * 删除地区公用物品
     *
     * @param itemId 物品ID
     * @return 是否成功
     */
    @Override
    @CacheEvict(value = "listCommonItem",allEntries = true)
    public Boolean deleteCommonItem(Long itemId) {
        return itemAreaPublicMapper.delete(Wrappers.<ItemAreaPublic>lambdaQuery()
                .eq(ItemAreaPublic::getItemId, itemId))
                == 1;
    }



    //---------------存储历史信息-------------------

    /**
     *
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
               dto->{
                   History history = HistoryConvert.convert(dto);
                   //存储入库
                   historyMapper.insert(history);
               }
        );
    }

}
