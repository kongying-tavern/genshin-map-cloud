package site.yuanshen.genshin.core.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
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
    public List<AreaDto> listArea(AreaSearchVo areaSearchVo) {
        //非递归查询
        if (!areaSearchVo.getIsTraverse()) {
            return areaMapper.selectList(Wrappers.<Area>lambdaQuery()
                            .eq(Area::getParentId, Optional.ofNullable(areaSearchVo.getParentId()).orElse(-1L)))
                    .stream().map(AreaDto::new).collect(Collectors.toList());
        }
        //递归用的临时ID列表
        List<Long> nowAreaIdList = Collections.singletonList(Optional.ofNullable(areaSearchVo.getParentId()).orElse(-1L));
        //存储查到的所有的地区信息
        List<AreaDto> result = new ArrayList<>();
        while (!nowAreaIdList.isEmpty()) {
            List<Area> areaList = areaMapper.selectList(Wrappers.<Area>lambdaQuery()
                    .in(Area::getParentId, nowAreaIdList));
            nowAreaIdList = areaList.parallelStream().map(Area::getId).collect(Collectors.toList());
            result.addAll(areaList.stream().map(AreaDto::new).collect(Collectors.toList()));
        }
        return result;
    }

    /**
     * 获取单个地区信息
     *
     * @param areaId 地区ID
     * @return 地区数据封装
     */
    @Override
    public AreaDto getArea(Long areaId) {
        return new AreaDto(areaMapper.selectOne(Wrappers.<Area>lambdaQuery()
                .eq(Area::getId, areaId)));
    }

    /**
     * 新增地区
     *
     * @param areaDto 地区数据封装
     * @return 新增地区ID
     */
    @Override
    public Long createArea(AreaDto areaDto) {
        Area area = areaDto.getEntity()
                //临时id
//				.setAreaId(-1L)
                .setIsFinal(true);
        areaMapper.insert(area);
//		//正式更新id
//		areaMapper.updateById(area.setAreaId(area.getId()));
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
    public Boolean updateArea(AreaDto areaDto) {
        //获取地区实体
        Area area = areaMapper.selectOne(Wrappers.<Area>lambdaQuery()
                .eq(Area::getId, areaDto.getAreaId()));
        //判断是否是末端地区
        area.setIsFinal(areaMapper.selectCount(Wrappers.<Area>lambdaQuery()
                .eq(Area::getParentId, areaDto.getAreaId()))
                > 0);
        //更新父级的末端标志
        if (!areaDto.getParentId().equals(area.getParentId())) {
            areaMapper.update(null, Wrappers.<Area>lambdaUpdate()
                    .eq(Area::getId, areaDto.getParentId())
                    .set(Area::getIsFinal, false));
            //更改原父级的末端标志(如果原父级只剩这个子级的话)
            if (areaMapper.selectCount(Wrappers.<Area>lambdaQuery()
                    .eq(Area::getParentId, areaDto.getAreaId()))
                    == 1) {
                areaMapper.update(null, Wrappers.<Area>lambdaUpdate()
                        .eq(Area::getId, area.getParentId())
                        .set(Area::getIsFinal, true));
            }
        }
        //更新实体
        return areaMapper.updateById(area) == 1;
    }

    /**
     * 递归删除地区
     *
     * @param areaId 地区ID
     * @return 是否成功
     */
    @Override
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
