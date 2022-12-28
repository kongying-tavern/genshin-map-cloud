package site.yuanshen.genshin.core.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import site.yuanshen.common.core.utils.BeanUtils;
import site.yuanshen.data.dto.AreaDto;
import site.yuanshen.data.entity.Area;
import site.yuanshen.data.entity.Item;
import site.yuanshen.data.entity.ItemAreaPublic;
import site.yuanshen.data.mapper.AreaMapper;
import site.yuanshen.data.mapper.ItemAreaPublicMapper;
import site.yuanshen.data.mapper.ItemMapper;
import site.yuanshen.data.vo.AreaSearchVo;
import site.yuanshen.genshin.core.service.AreaService;
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
public class AreaServiceImpl implements AreaService {

    private final AreaMapper areaMapper;
    private final AreaMBPService areaMBPService;
    private final ItemAreaPublicMapper itemAreaPublicMapper;
    private final ItemMapper itemMapper;
    private final ItemMBPService itemMBPService;

    /**
     * 列出地区
     *
     * @param areaSearchVo 地区查询VO
     * @return 地区数据封装列表
     */
    @Override
    @Cacheable(value = "listArea")
    public List<AreaDto> listArea(AreaSearchVo areaSearchVo) {
        //非递归查询
        if (!areaSearchVo.getIsTraverse()) {
            //如果不为测试打点员,则搜索时hiddenFlag!=2
            return areaMapper.selectList(Wrappers.<Area>lambdaQuery().in(!areaSearchVo.getHiddenFlagList().isEmpty(),Area::getHiddenFlag,areaSearchVo.getHiddenFlagList())
                            .eq(Area::getParentId, Optional.ofNullable(areaSearchVo.getParentId()).orElse(-1L)))
                    .stream().map(AreaDto::new).collect(Collectors.toList());
        }
        //递归用的临时ID列表
        List<Long> nowAreaIdList = Collections.singletonList(Optional.ofNullable(areaSearchVo.getParentId()).orElse(-1L));
        //存储查到的所有的地区信息
        List<AreaDto> result = new ArrayList<>();
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
     * @return 地区数据封装
     */
    @Override
    @Cacheable(value = "area",key = "#areaId")
    public AreaDto getArea(Long areaId,List<Integer> hiddenFlagList) {
        return new AreaDto(areaMapper.selectOne(Wrappers.<Area>lambdaQuery().in(!hiddenFlagList.isEmpty(),Area::getHiddenFlag,hiddenFlagList)
                .eq(Area::getId, areaId)));
    }

    /**
     * 新增地区
     *
     * @param areaDto 地区数据封装
     * @return 新增地区ID
     */
    @Override
    @Transactional
    @Caching(
            evict = {
                    @CacheEvict(value = "area", key = "#result"),
                    @CacheEvict(value = "listArea", allEntries = true)
            }
    )
    public Long createArea(AreaDto areaDto) {
        Area area = areaDto.getEntity()
                .setIsFinal(true);
        areaMapper.insert(area);
        //插入公共物品
        List<Long> commonItemIdList = itemAreaPublicMapper.selectList(Wrappers.<ItemAreaPublic>lambdaQuery())
                .parallelStream().map(ItemAreaPublic::getItemId).collect(Collectors.toList());
        if (commonItemIdList.size() != 0) {
            List<Item> commonItemList = itemMapper.selectList(Wrappers.<Item>lambdaQuery().in(Item::getId, commonItemIdList));
            //临时id
            long tempId = -1L;
            for (Item item : commonItemList) {
                item.setId(null);
//				item.setItemId(tempId--);
                item.setAreaId(area.getId());
            }
            //TODO 异常处理，第一次出错隔10+random(5)值重试
            itemMBPService.saveBatch(commonItemList);
            //正式更新id
//			for (Item item : commonItemList) {
//				item.setItemId(item.getId());
//			}
            itemMBPService.updateBatchById(commonItemList);
        }
        return area.getId();
    }

    /**
     * 修改地区
     *
     * @param areaDto 地区数据封装
     * @return 是否成功
     */
    @Override
    @Transactional
    @Caching(
            evict = {
                    @CacheEvict(value = "area", key = "#areaDto.areaId"),
                    @CacheEvict(value = "listArea", allEntries = true)
            }
    )
    public Boolean updateArea(AreaDto areaDto) {
        //获取地区实体
        Area area = areaMapper.selectOne(Wrappers.<Area>lambdaQuery()
                .eq(Area::getId, areaDto.getAreaId()));
        //更新父级的末端标志
        if (!areaDto.getParentId().equals(area.getParentId())) {
            areaMapper.update(null, Wrappers.<Area>lambdaUpdate()
                        .eq(Area::getId, areaDto.getParentId())
                    .set(Area::getIsFinal, false));
            //更改原父级的末端标志(如果原父级只剩这个子级的话)
            if (areaMapper.selectCount(Wrappers.<Area>lambdaQuery()
                    .eq(Area::getParentId, area.getParentId()))
                    == 1) {
                areaMapper.update(null, Wrappers.<Area>lambdaUpdate()
                        .eq(Area::getId, area.getParentId())
                        .set(Area::getIsFinal, true));
            }
        }
        if (areaDto.getAreaId().equals(areaDto.getParentId())) {
            throw new RuntimeException("地区ID不允许与父ID相同，会造成自身父子");
        }
        //更新实体
        BeanUtils.copyNotNull(areaDto.getEntity(),area);
        //判断是否是末端地区
        area.setIsFinal(areaMapper.selectCount(Wrappers.<Area>lambdaQuery()
                .eq(Area::getParentId, areaDto.getAreaId()))
                == 0);
        return areaMapper.updateById(area) == 1;
    }

    /**
     * 递归删除地区
     *
     * @param areaId 地区ID
     * @return 是否成功
     */
    @Override
    @Transactional
    @Caching(
            evict = {
                    @CacheEvict(value = "area", key = "#areaId"),
                    @CacheEvict(value = "listArea", allEntries = true)
            }
    )
    public Boolean deleteArea(Long areaId) {
        //用于递归遍历删除的地区ID列表
        List<Long> nowAreaIdList = Collections.singletonList(areaId);
        //todo 删除绑定的物品和点位
        //todo 检测地区共有物品是否有物品在此地区
        while (!nowAreaIdList.isEmpty()) {
            areaMapper.delete(Wrappers.<Area>lambdaQuery().in(Area::getId, nowAreaIdList));
            nowAreaIdList = areaMapper.selectList(Wrappers.<Area>lambdaQuery().in(Area::getParentId, nowAreaIdList))
                    .parallelStream().map(Area::getId).collect(Collectors.toList());
        }
        return true;
    }
}
