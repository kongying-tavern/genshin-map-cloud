package site.yuanshen.genshin.core.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import site.yuanshen.data.dto.ItemDto;
import site.yuanshen.data.dto.helper.PageSearchDto;
import site.yuanshen.data.entity.Item;
import site.yuanshen.data.entity.ItemAreaPublic;
import site.yuanshen.data.mapper.ItemAreaPublicMapper;
import site.yuanshen.data.mapper.ItemMapper;
import site.yuanshen.data.vo.ItemVo;
import site.yuanshen.data.vo.helper.PageListVo;
import site.yuanshen.genshin.core.service.ItemCommonService;
import site.yuanshen.genshin.core.service.mbp.ItemAreaPublicMBPService;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
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
    @Transactional
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
    @Transactional
    public Boolean deleteCommonItem(Long itemId) {
        return itemAreaPublicMapper.delete(Wrappers.<ItemAreaPublic>lambdaQuery()
                .eq(ItemAreaPublic::getItemId, itemId))
                == 1;
    }
}
