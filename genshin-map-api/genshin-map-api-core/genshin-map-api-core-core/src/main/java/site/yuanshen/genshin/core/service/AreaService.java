package site.yuanshen.genshin.core.service;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import site.yuanshen.common.core.exception.GenshinApiException;
import site.yuanshen.common.core.utils.BeanUtils;
import site.yuanshen.data.dto.AreaDto;
import site.yuanshen.data.entity.Area;
import site.yuanshen.data.entity.Item;
import site.yuanshen.data.entity.ItemAreaPublic;
import site.yuanshen.data.entity.MarkerItemLink;
import site.yuanshen.data.mapper.*;
import site.yuanshen.data.vo.AreaSearchVo;
import site.yuanshen.genshin.core.service.mbp.AreaMBPService;
import site.yuanshen.genshin.core.service.mbp.ItemMBPService;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 地区服务接口实现
 *
 * @author Moment
 */
@Service
@RequiredArgsConstructor
public class AreaService {

    private final AreaMapper areaMapper;
    private final AreaMBPService areaMBPService;
    private final ItemAreaPublicMapper itemAreaPublicMapper;
    private final ItemMapper itemMapper;
    private final ItemMBPService itemMBPService;
    private final MarkerItemLinkMapper markerItemLinkMapper;
    private final MarkerMapper markerMapper;

    /**
     * 列出地区
     *
     * @param areaSearchVo 地区查询VO
     * @return 地区数据封装列表
     */
    @Cacheable(value = "listArea")
    public List<AreaDto> listArea(AreaSearchVo areaSearchVo) {
        List<AreaDto> result = new ArrayList<>();

        //非递归查询
        if (!areaSearchVo.getIsTraverse()) {
            //如果不为测试打点员,则搜索时hiddenFlag!=2
            result = areaMapper.selectList(Wrappers.<Area>lambdaQuery().in(!areaSearchVo.getHiddenFlagList().isEmpty(),Area::getHiddenFlag,areaSearchVo.getHiddenFlagList())
                            .eq(Area::getParentId, Optional.ofNullable(areaSearchVo.getParentId()).orElse(-1L)))
                    .stream().map(AreaDto::new)
                    .sorted(Comparator.comparing(AreaDto::getSortIndex).reversed())
                    .collect(Collectors.toList());
            return result;
        }
        //递归用的临时ID列表
        List<Long> nowAreaIdList = Collections.singletonList(Optional.ofNullable(areaSearchVo.getParentId()).orElse(-1L));

        //存储查到的所有的地区信息
        while (!nowAreaIdList.isEmpty()) {
            List<Area> areaList = areaMapper.selectList(Wrappers.<Area>lambdaQuery().in(!areaSearchVo.getHiddenFlagList().isEmpty(),Area::getHiddenFlag,areaSearchVo.getHiddenFlagList())
                    .in(Area::getParentId, nowAreaIdList));
            nowAreaIdList = areaList.parallelStream().map(Area::getId).collect(Collectors.toList());
            result.addAll(areaList.stream().map(AreaDto::new).collect(Collectors.toList()));
        }
        return result.stream().sorted(Comparator.comparing(AreaDto::getSortIndex).reversed()).collect(Collectors.toList());
    }

    /**
     * 获取单个地区信息
     *
     * @param areaId 地区ID
     * @param hiddenFlagList 显隐等级List
     * @return 地区数据封装
     */
    @Cacheable(value = "area",key = "#areaId+'*'+#hiddenFlagList")
    public AreaDto getArea(Long areaId, List<Integer> hiddenFlagList) {
        return new AreaDto(areaMapper.selectOne(Wrappers.<Area>lambdaQuery().in(!hiddenFlagList.isEmpty(),Area::getHiddenFlag,hiddenFlagList)
                .eq(Area::getId, areaId)));
    }

    /**
     * 新增地区
     *
     * @param areaDto 地区数据封装
     * @return 新增地区ID
     */
    @Transactional
    @Caching(
            evict = {
                    @CacheEvict(value = "area", key = "#result"),
                    @CacheEvict(value = "listArea", allEntries = true)
            }
    )
    public Long createArea(AreaDto areaDto) {
        if (Objects.equals(areaDto.getId(), areaDto.getParentId())) {
            throw new GenshinApiException("地区ID不允许与父ID相同，会造成自身父子");
        }

        Area area = areaDto.getEntity()
                .withIsFinal(true);
        areaMapper.insert(area);
        //插入公共物品
        List<Long> commonItemIdList = itemAreaPublicMapper.selectList(Wrappers.<ItemAreaPublic>lambdaQuery())
                .parallelStream().map(ItemAreaPublic::getItemId).collect(Collectors.toList());
        if (commonItemIdList.size() != 0) {
            List<Item> commonItemList = itemMapper.selectList(Wrappers.<Item>lambdaQuery().in(Item::getId, commonItemIdList));
            for (Item item : commonItemList) {
                item.setId(null);
                item.setAreaId(area.getId());
            }
            itemMBPService.saveBatch(commonItemList);
            itemMBPService.updateBatchById(commonItemList);
        }

        //更新父级的末端标志
        updateAreaIsFinal(areaDto.getParentId(), false);
        return area.getId();
    }

    /**
     * 修改地区
     *
     * @param areaDto 地区数据封装
     * @return 是否成功
     */
    @Transactional
    @Caching(
            evict = {
                    @CacheEvict(value = "area", key = "#areaDto.id"),
                    @CacheEvict(value = "listArea", allEntries = true)
            }
    )
    public Boolean updateArea(AreaDto areaDto) {
        if (Objects.equals(areaDto.getId(), areaDto.getParentId())) {
            throw new GenshinApiException("地区ID不允许与父ID相同，会造成自身父子");
        }

        //获取地区实体
        Area area = areaMapper.selectOne(Wrappers.<Area>lambdaQuery()
                .eq(Area::getId, areaDto.getId()));
        //更新父级的末端标志
        if (!Objects.equals(area.getParentId(), areaDto.getParentId())) {
            // 更改新父级的末端标识
            updateAreaIsFinal(areaDto.getParentId(), false);
            //更改原父级的末端标志(如果原父级只剩这个子级的话)
            recalculateAreaIsFinal(area.getParentId(), true);
        }
        //更新实体
        BeanUtils.copyNotNull(areaDto.getEntity(),area);
        updateAreaIsFinal(area);
        return areaMapper.updateById(area) == 1;
    }

    /**
     * 递归删除地区
     *
     * @param areaId 地区ID
     * @return 是否成功
     */
    @Transactional
    @Caching(
            evict = {
                    @CacheEvict(value = "area", key = "#areaId"),
                    @CacheEvict(value = "listArea", allEntries = true)
            }
    )
    public Boolean deleteArea(Long areaId) {
        final Long parentAreaId = areaMapper.selectById(areaId).getParentId();

        //用于递归遍历删除的地区ID列表
        List<Long> nowAreaIdList = Collections.singletonList(areaId);
        while (!nowAreaIdList.isEmpty()) {
            areaMapper.delete(Wrappers.<Area>lambdaQuery().in(Area::getId, nowAreaIdList));
            deleteMarkerAndItemInArea(nowAreaIdList);
            nowAreaIdList = areaMapper.selectList(Wrappers.<Area>lambdaQuery().in(Area::getParentId, nowAreaIdList))
                    .parallelStream().map(Area::getId).collect(Collectors.toList());
        }

        //更新父级标记
        recalculateAreaIsFinal(parentAreaId, false);

        return true;
    }

    private void updateAreaIsFinal(Long parentId, boolean isFinal) {
        if(parentId != null && parentId > 0L) {
            areaMapper.update(null, Wrappers.<Area>lambdaUpdate()
                .eq(Area::getId, parentId)
                .set(Area::getIsFinal, isFinal));
        }
    }

    private void updateAreaIsFinal(Area area) {
        if(area != null) {
            area.setIsFinal(areaMapper.selectCount(Wrappers.<Area>lambdaQuery()
                .eq(Area::getParentId, area.getId()))
                == 0);
        }
    }

    private void recalculateAreaIsFinal(Long parentId, boolean beforeModify) {
        if(parentId != null) {
            if (
                areaMapper.selectCount(Wrappers.<Area>lambdaQuery()
                    .eq(Area::getParentId, parentId))
                    == (beforeModify ? 1 : 0)
            ) {
                areaMapper.update(null, Wrappers.<Area>lambdaUpdate()
                        .eq(Area::getId, parentId)
                        .set(Area::getIsFinal, true));
            } else {
                areaMapper.update(null, Wrappers.<Area>lambdaUpdate()
                        .eq(Area::getId, parentId)
                        .set(Area::getIsFinal, false));
            }
        }
    }

    private void deleteMarkerAndItemInArea(List<Long> areaIdList) {
        //选取对应的物品id
        List<Long> itemIdList = itemMapper.selectObjs(Wrappers.<Item>lambdaQuery()
                        .select(Item::getId)
                        .in(Item::getAreaId, areaIdList))
                .parallelStream().map(o -> (Long) o).collect(Collectors.toList());
        if (itemIdList.isEmpty())
            return;
        //删除物品
        itemMapper.deleteBatchIds(areaIdList);
        //选取对应点位id
        List<Long> markerIdList = markerItemLinkMapper.selectObjs(Wrappers.<MarkerItemLink>lambdaQuery().select(MarkerItemLink::getMarkerId).in(MarkerItemLink::getItemId, itemIdList)).parallelStream().map(o -> (Long) o).collect(Collectors.toList());
        if (markerIdList.isEmpty())
            return;
        //先删除点位-物品关联
        markerItemLinkMapper.delete(Wrappers.<MarkerItemLink>lambdaQuery().in(MarkerItemLink::getItemId, itemIdList));
        //筛选出关联其他地区物品的点位，其他的删除
        markerItemLinkMapper.selectObjs(Wrappers.<MarkerItemLink>lambdaQuery()
                        .select(MarkerItemLink::getMarkerId)
                        .in(MarkerItemLink::getMarkerId, markerIdList))
                .parallelStream().map(o -> (Long) o).forEach(markerIdList::remove);
        markerMapper.deleteBatchIds(markerIdList);
    }
}
