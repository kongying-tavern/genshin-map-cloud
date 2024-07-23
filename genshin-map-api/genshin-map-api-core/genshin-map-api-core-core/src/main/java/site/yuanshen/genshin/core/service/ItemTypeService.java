package site.yuanshen.genshin.core.service;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import site.yuanshen.common.core.exception.GenshinApiException;
import site.yuanshen.common.core.utils.BeanUtils;
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
import java.util.Objects;
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
        List<ItemTypeVo> result = itemTypePage.getRecords().stream()
                .map(ItemTypeDto::new).map(ItemTypeDto::getVo)
                .sorted(Comparator.comparing(ItemTypeVo::getSortIndex).reversed())
                .collect(Collectors.toList());
        return new PageListVo<ItemTypeVo>()
                .setRecord(result)
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
        List<ItemTypeVo> result = itemTypeMapper.selectList(Wrappers.<ItemType>lambdaQuery()
                        .in(!hiddenFlagList.isEmpty(), ItemType::getHiddenFlag, hiddenFlagList))
                .stream()
                .map(ItemTypeDto::new)
                .map(ItemTypeDto::getVo)
                .collect(Collectors.toList());
        return result;
    }

    /**
     * 添加物品类型
     *
     * @param itemTypeDto 物品类型数据封装
     * @return 新物品类型ID
     */
    @Transactional
    public Long addItemType(ItemTypeDto itemTypeDto) {
        if (Objects.equals(itemTypeDto.getId(), itemTypeDto.getParentId())) {
            throw new GenshinApiException("物品类型ID不允许与父ID相同，会造成自身父子");
        }

        ItemType itemType = itemTypeDto.getEntity()
            .withIsFinal(true);
        itemTypeMapper.insert(itemType);

        //更新父级的末端标志
        updateItemTypeIsFinal(itemTypeDto.getParentId(), false);

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
        if (Objects.equals(itemTypeDto.getId(), itemTypeDto.getParentId())) {
            throw new GenshinApiException("物品类型ID不允许与父ID相同，会造成自身父子");
        }

        //获取类型实体
        ItemType itemType = itemTypeMapper.selectOne(Wrappers.<ItemType>lambdaQuery()
                .eq(ItemType::getId, itemTypeDto.getId()));
        //更新父级的末端标志
        if (!Objects.equals(itemType.getParentId(), itemTypeDto.getParentId())) {
            // 更改新父级的末端标识
            updateItemTypeIsFinal(itemTypeDto.getParentId(), false);
            //更改原父级的末端标志(如果原父级只剩这个子级的话)
            recalculateItemTypeIsFinal(itemType.getParentId(), true);
        }
        //更新实体
        BeanUtils.copyNotNull(itemTypeDto.getEntity(), itemType);
        //判断是否是末端地区
        updateItemTypeIsFinal(itemType);
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
        if(CollUtil.contains(itemTypeIdList, targetTypeId)) {
            throw new GenshinApiException("物品类型ID不允许与父ID相同，会造成自身父子");
        }

        // 获取实体
        List<ItemType> itemTypeList = itemTypeMapper.selectList(Wrappers.<ItemType>lambdaQuery()
                .in(ItemType::getId, itemTypeIdList));
        if(CollUtil.isEmpty(itemTypeList))
            return true;
        List<Long> parentIdList = itemTypeList.parallelStream().map(ItemType::getParentId).distinct().collect(Collectors.toList());
        //更新父级
        updateItemTypeIsFinal(targetTypeId, false);
        //更新实体
        itemTypeList = itemTypeList.parallelStream().peek(itemType -> itemType.setParentId(targetTypeId)).collect(Collectors.toList());
        itemTypeMBPService.updateBatchById(itemTypeList);
        //更新原父级
        parentIdList.parallelStream().forEach(parentId -> {
            recalculateItemTypeIsFinal(parentId, false);
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
        final Long parentItemTypeId = itemTypeMapper.selectById(itemTypeId).getParentId();

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

        //更新父级标记
        recalculateItemTypeIsFinal(parentItemTypeId, false);

        return true;
    }

    private void updateItemTypeIsFinal(Long parentId, boolean isFinal) {
            if(parentId != null && parentId > 0L) {
            itemTypeMapper.update(null, Wrappers.<ItemType>lambdaUpdate()
                .eq(ItemType::getId, parentId)
                .set(ItemType::getIsFinal, isFinal));
        }
    }

    private void updateItemTypeIsFinal(ItemType itemType) {
        if(itemType != null) {
            itemType.setIsFinal(itemTypeMapper.selectCount(Wrappers.<ItemType>lambdaQuery()
                .eq(ItemType::getParentId, itemType.getId()))
                == 0);
        }
    }

    private void recalculateItemTypeIsFinal(Long parentId, boolean beforeModify) {
        if(parentId != null) {
            if (
                itemTypeMapper.selectCount(Wrappers.<ItemType>lambdaQuery()
                    .eq(ItemType::getParentId, parentId))
                    == (beforeModify ? 1 : 0)
            ) {
                itemTypeMapper.update(null, Wrappers.<ItemType>lambdaUpdate()
                        .eq(ItemType::getId, parentId)
                        .set(ItemType::getIsFinal, true));
            } else {
                itemTypeMapper.update(null, Wrappers.<ItemType>lambdaUpdate()
                        .eq(ItemType::getId, parentId)
                        .set(ItemType::getIsFinal, false));
            }
        }
    }

}
