package site.yuanshen.genshin.core.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import site.yuanshen.data.dto.ItemAreaPublicDto;
import site.yuanshen.data.dto.ItemDto;
import site.yuanshen.data.dto.helper.PageSearchDto;
import site.yuanshen.data.entity.Item;
import site.yuanshen.data.entity.ItemAreaPublic;
import site.yuanshen.data.mapper.ItemAreaPublicMapper;
import site.yuanshen.data.mapper.ItemMapper;
import site.yuanshen.data.vo.ItemAreaPublicVo;
import site.yuanshen.data.vo.ItemVo;
import site.yuanshen.data.vo.helper.PageListVo;
import site.yuanshen.genshin.core.service.ItemCommonService;
import site.yuanshen.genshin.core.service.mbp.ItemAreaPublicMBPService;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 公共物品服务接口实现
 *
 * @author Alex Fang
 */
@Service
@RequiredArgsConstructor
public class ItemCommonServiceImpl implements ItemCommonService {

    private final ItemMapper itemMapper;
    private final ItemAreaPublicMapper itemAreaPublicMapper;
    private final ItemAreaPublicMBPService itemAreaPublicMBPService;

    /**
     * 列出地区公用物品
     *
     * @param pageSearchDto 分页查询数据封装
     * @return 物品前端封装的分页封装
     */
    @Override
    @Cacheable("listCommonItem")
    public PageListVo<ItemAreaPublicVo> listCommonItem(PageSearchDto pageSearchDto) {
        //取公共物品实体
        Page<ItemAreaPublic> itemPublicPage = itemAreaPublicMapper.selectPage(pageSearchDto.getPageEntity(), Wrappers.<ItemAreaPublic>query());
        List<ItemAreaPublic> itemPublicList = itemPublicPage.getRecords();
        if (itemPublicList.isEmpty()) {
            return new PageListVo<>(new ArrayList<>(), itemPublicPage.getTotal(), itemPublicPage.getSize());
        }
        //取物品具体信息
        Map<Long, ItemDto> itemMap = itemMapper.selectList(Wrappers.<Item>lambdaQuery()
                        .in(Item::getId, itemPublicList.parallelStream()
                                .map(ItemAreaPublic::getItemId).collect(Collectors.toList())))
                .parallelStream().map(ItemDto::new).collect(Collectors.toMap(ItemDto::getId, item -> item));
        //组合VO
        return new PageListVo<ItemAreaPublicVo>()
                .setRecord(itemPublicList.parallelStream()
                        .map(ItemAreaPublicDto::new)
                        .map(dto -> dto.withItemDto(itemMap.get(dto.getAreaId())))
                        .map(ItemAreaPublicDto::getVo)
                        .sorted(Comparator.comparingLong(ItemAreaPublicVo::getId))
                        .collect(Collectors.toList()))
                .setTotal(itemPublicPage.getTotal())
                .setSize(itemPublicPage.getSize());
    }

    /**
     * 新增地区公用物品
     *
     * @param itemIdList 物品ID列表
     * @return 是否成功
     */
    @Override
    @CacheEvict(value = "listCommonItem", allEntries = true)
    @Transactional
    public Boolean addCommonItem(List<Long> itemIdList) {
        //获取存在的全部名字
        List<Long> itemAreaList = itemAreaPublicMBPService.list().stream().map(ItemAreaPublic::getItemId).collect(Collectors.toList());
        List<String> itemNameList = itemAreaList.isEmpty() ? new ArrayList<>() : itemMapper.selectList(Wrappers.<Item>lambdaQuery()
                        .in(Item::getId, itemAreaList))
                .stream().map(Item::getName).collect(Collectors.toList());
        //查询出新增列表全部的名字 并根据名字分组 过滤出不存在的名字 并取第一个物品id
        List<Long> itemList = itemMapper.selectList(Wrappers.<Item>lambdaQuery()
                        .in(Item::getId, itemIdList))
                .stream().filter(item -> !itemNameList.contains(item.getName()))
                .collect(Collectors.groupingBy(Item::getName))
                .values()
                .stream().map(items -> items.get(0).getId()).collect(Collectors.toList());


        return itemAreaPublicMBPService.saveBatch(itemList.parallelStream()
                .map(id -> new ItemAreaPublic()
                        .withItemId(id))
                .collect(Collectors.toList()));
    }

    /**
     * 删除地区公用物品
     *
     * @param itemId 物品ID
     * @return 是否成功
     */
    @Override
    @CacheEvict(value = "listCommonItem", allEntries = true)
    @Transactional
    public Boolean deleteCommonItem(Long itemId) {
        return itemAreaPublicMapper.delete(Wrappers.<ItemAreaPublic>lambdaQuery()
                .eq(ItemAreaPublic::getItemId, itemId))
                == 1;
    }
}
