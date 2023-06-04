package site.yuanshen.genshin.core.service;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import site.yuanshen.data.dto.ItemTypeDto;
import site.yuanshen.data.dto.helper.PageAndTypeSearchDto;
import site.yuanshen.data.entity.ItemType;
import site.yuanshen.data.entity.ItemTypeLink;
import site.yuanshen.data.mapper.ItemTypeLinkMapper;
import site.yuanshen.data.mapper.ItemTypeMapper;
import site.yuanshen.data.vo.ItemTypeVo;
import site.yuanshen.data.vo.helper.PageListVo;
import site.yuanshen.genshin.core.service.mbp.ItemTypeMBPService;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 物品分类服务接口实现
 *
 * @author Alex Fang
 */
@Service
@RequiredArgsConstructor
public class ItemTypeService {

    private final ItemTypeMapper itemTypeMapper;
    private final ItemTypeMBPService itemTypeMBPService;
    private final ItemTypeLinkMapper itemTypeLinkMapper;

    /**
     * 列出物品类型
     *
     * @param searchDto 带分类的分页查询数据封装
     * @param self      查询自身还是查询子级，0为查询自身，1为查询子级
     * @return 物品类型的前端封装的分页封装
     */
    @Cacheable("listItemType")
    public PageListVo<ItemTypeVo> listItemType(PageAndTypeSearchDto searchDto, Integer self, List<Integer> hiddenFlagList) {
        Page<ItemType> itemTypePage = new Page<>();
        //查询自身
        List<Long> typeIdList = searchDto.getTypeIdList();
        if (self.equals(0)) {
            if (typeIdList != null && typeIdList.size() > 0) {
                itemTypePage = itemTypeMapper.selectPage(searchDto.getPageEntity(), Wrappers.<ItemType>lambdaQuery()
                        .in(!hiddenFlagList.isEmpty(),ItemType::getHiddenFlag,hiddenFlagList)
                        .in(ItemType::getId, typeIdList));
            } else {
                itemTypePage.setTotal(0L);
            }
        }
        //查询子级
        else if (self.equals(1)) {
            itemTypePage = itemTypeMapper.selectPage(searchDto.getPageEntity(), Wrappers.<ItemType>lambdaQuery()
                    .in(!hiddenFlagList.isEmpty(),ItemType::getHiddenFlag,hiddenFlagList)
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
     * 列出所有物品类型
     *
     * @param hiddenFlagList hidden_flag范围
     * @return 物品类型的前端封装的列表
     */
    @Cacheable("listAllItemType")
    public List<ItemTypeVo> listAllItemType(List<Integer> hiddenFlagList) {
        return itemTypeMapper.selectList(Wrappers.<ItemType>lambdaQuery()
                        .in(!hiddenFlagList.isEmpty(), ItemType::getHiddenFlag, hiddenFlagList))
                .stream()
                .map(ItemTypeDto::new)
                .map(ItemTypeDto::getVo)
                .collect(Collectors.toList());
    }

    /**
     * 添加物品类型
     *
     * @param itemTypeDto 物品类型数据封装
     * @return 新物品类型ID
     */
    @Transactional
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
        return itemType.getId();
    }

    /**
     * 修改物品类型
     *
     * @param itemTypeDto 物品类型数据封装
     * @return 是否成功
     */
    @Transactional
    public Boolean updateItemType(ItemTypeDto itemTypeDto) {
        //获取类型实体
        ItemType itemType = itemTypeMapper.selectOne(Wrappers.<ItemType>lambdaQuery()
                .eq(ItemType::getId, itemTypeDto.getId()));
        //设置内容 TODO 检查所有的set是否包含了所有的属性 或者直接替换成属性复制工具
        itemType.setIconTag(itemTypeDto.getIconTag());
        itemType.setName(itemTypeDto.getName());
        itemType.setContent(itemTypeDto.getContent());
        //判断是否是末端类型
        itemType.setIsFinal(
                itemTypeMapper.selectCount(Wrappers.<ItemType>lambdaQuery()
                        .eq(ItemType::getParentId, itemType.getId()))
                        <= 0);
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
            } else {
                itemTypeMapper.update(null, Wrappers.<ItemType>lambdaUpdate()
                        .eq(ItemType::getId, itemType.getParentId())
                        .set(ItemType::getIsFinal, false));
            }
            itemType.setParentId(itemTypeDto.getParentId());
        }
        //更新实体
        itemTypeMapper.updateById(itemType);
        return true;
    }

    /**
     * 批量移动类型为目标类型的子类型
     *
     * @param itemTypeIdList 类型ID列表
     * @param targetTypeId   目标类型ID
     * @return 是否成功
     */
    @Transactional
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
        return true;
    }

    /**
     * 批量递归删除物品类型
     *
     * @param itemTypeId 类型ID列表
     * @return 是否成功
     */
    @Transactional
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
        return true;
    }

}
