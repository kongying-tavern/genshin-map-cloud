package site.yuanshen.genshin.core.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import site.yuanshen.data.dto.IconDto;
import site.yuanshen.data.dto.IconSearchDto;
import site.yuanshen.data.dto.IconTypeDto;
import site.yuanshen.data.dto.helper.PageAndTypeListDto;
import site.yuanshen.data.entity.Icon;
import site.yuanshen.data.entity.IconType;
import site.yuanshen.data.entity.IconTypeLink;
import site.yuanshen.data.mapper.IconMapper;
import site.yuanshen.data.mapper.IconTypeLinkMapper;
import site.yuanshen.data.mapper.IconTypeMapper;
import site.yuanshen.data.vo.IconTypeVo;
import site.yuanshen.data.vo.IconVo;
import site.yuanshen.data.vo.helper.PageListVo;
import site.yuanshen.genshin.core.service.IconService;
import site.yuanshen.genshin.core.service.mbp.IconMBPService;
import site.yuanshen.genshin.core.service.mbp.IconTypeLinkMBPService;
import site.yuanshen.genshin.core.service.mbp.IconTypeMBPService;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * 图标服务接口实现
 *
 * @author Moment
 */
@Service
@RequiredArgsConstructor
public class IconServiceImpl implements IconService {

    private final IconMapper iconMapper;
    private final IconMBPService iconMBPService;
    private final IconTypeMapper iconTypeMapper;
    private final IconTypeMBPService iconTypeMBPService;
    private final IconTypeLinkMapper iconTypeLinkMapper;
    private final IconTypeLinkMBPService iconTypeLinkMBPService;

    /**
     * 列出图标
     *
     * @param searchDto 图标分页查询VO
     * @return 图标前端对象列表
     */
    @Override
    public PageListVo<IconVo> listIcon(IconSearchDto searchDto) {
        Page<Icon> iconPage = iconMapper.selectPageIcon(searchDto.getPageEntity(), searchDto);
        //按照条件进行筛选
        List<IconDto> iconDtoList = iconPage
                .getRecords()
                .stream()
                .map(IconDto::new)
                .collect(Collectors.toList());
        //收集分类信息
        Map<Long, List<Long>> typeMap = new ConcurrentHashMap<>();
        iconTypeLinkMapper.selectList(Wrappers.<IconTypeLink>lambdaQuery()
                        .in(IconTypeLink::getIconId, iconDtoList.parallelStream()
                                .map(IconDto::getIconId).collect(Collectors.toList())))
                //TODO 验证此处并行是否会有bug
                .parallelStream()
                .forEach(typeLink ->
                        typeMap.compute(typeLink.getIconId(), (iconId, typeList) -> {
                            if (typeList == null) return Collections.singletonList(typeLink.getTypeId());
                            typeList.add(typeLink.getTypeId());
                            return typeList;
                        }));
        //写入分类信息
        return new PageListVo<IconVo>()
                .setRecord(iconDtoList.stream().map(dto ->
                                dto.setTypeIdList(typeMap.getOrDefault(dto.getIconId(), new ArrayList<>()))
                                        .getVo())
                        .collect(Collectors.toList()))
                .setSize(iconPage.getSize())
                .setTotal(iconPage.getTotal());
    }

    /**
     * 获取单个图标信息
     *
     * @param iconId 图标ID
     * @return 图标前端对象
     */
    @Override
    public IconDto getIcon(Long iconId) {
        //获取类型信息
        List<Long> typeIdList = iconTypeLinkMapper.selectList(Wrappers.<IconTypeLink>lambdaQuery()
                        .eq(IconTypeLink::getIconId, iconId)).stream()
                .map(IconTypeLink::getTypeId).collect(Collectors.toList());
        return new IconDto(
                iconMapper.selectOne(Wrappers.<Icon>lambdaQuery()
                        .eq(Icon::getIconId, iconId))
        ).setTypeIdList(typeIdList);
    }

    /**
     * 修改图标信息
     *
     * @param iconDto 图标前端对象
     * @return 是否成功
     */
    @Override
    public Boolean updateIcon(IconDto iconDto) {
        //对比类型信息是否更改
        Set<Long> oldTypeIds = iconTypeLinkMapper.selectList(Wrappers.<IconTypeLink>lambdaQuery()
                        .eq(IconTypeLink::getIconId, iconDto.getIconId()))
                .stream()
                .map(IconTypeLink::getTypeId).collect(Collectors.toSet());
        Set<Long> newTypeIds = new TreeSet<>(iconDto.getTypeIdList());
        //如果更改了就进行分类的刷新
        if (!oldTypeIds.equals(newTypeIds)) {
            iconTypeLinkMapper.delete(Wrappers.<IconTypeLink>lambdaQuery()
                    .eq(IconTypeLink::getIconId, iconDto.getIconId()));
            iconTypeLinkMBPService.saveBatch(
                    newTypeIds.stream()
                            .map(id -> new IconTypeLink()
                                    .setIconId(iconDto.getIconId())
                                    .setTypeId(id))
                            .collect(Collectors.toList())
            );
        }
        //更新实体信息
        return iconMapper.update(iconDto.getEntity(),
                Wrappers.<Icon>lambdaUpdate()
                        .eq(Icon::getIconId, iconDto.getIconId()))
                == 1;
    }

    /**
     * 新增图标
     *
     * @param iconDto 图标前端对象
     * @return 新图标的ID
     */
    @Override
    public Long createIcon(IconDto iconDto) {
        Icon icon = iconDto.getEntity()
                //临时id
                .setIconId(-1L);
        iconMapper.insert(icon);
        //正式更新id
        iconMapper.updateById(icon.setIconId(icon.getId()));
        //处理类型信息
        List<Long> typeIdList = iconDto.getTypeIdList();
        if (typeIdList != null) {
            //判断是否有不存在的类型ID
            if (typeIdList.size() != iconTypeMapper.selectCount(Wrappers.<IconType>lambdaQuery().in(IconType::getId, typeIdList)))
                throw new RuntimeException("类型ID错误");
            iconTypeLinkMBPService.saveBatch(
                    typeIdList.stream()
                            .map(id -> new IconTypeLink().setIconId(iconDto.getIconId()).setTypeId(id))
                            .collect(Collectors.toList())
            );
        }
        return icon.getIconId();
    }

    /**
     * 删除图标
     *
     * @param iconId 图标ID
     * @return 是否成功
     */
    @Override
    public Boolean deleteIcon(Long iconId) {
        iconTypeLinkMapper.delete(Wrappers.<IconTypeLink>lambdaQuery()
                .eq(IconTypeLink::getIconId, iconId));
        return iconMapper.delete(Wrappers.<Icon>lambdaQuery()
                .eq(Icon::getIconId, iconId))
                == 1;
    }

    /**
     * 列出分类
     *
     * @param searchDto 带遍历的分页查询DTO
     * @return 图标类型列表
     */
    @Override
    public PageListVo<IconTypeVo> listIconType(PageAndTypeListDto searchDto) {
        Page<IconType> iconTypePage = iconTypeMapper.selectPage(searchDto.getPageEntity(),
                Wrappers.<IconType>lambdaQuery()
                        .in(IconType::getParent,
                                Optional.ofNullable(searchDto.getTypeIdList())
                                        .orElse(Collections.singletonList(-1L))));
        return new PageListVo<IconTypeVo>()
                .setRecord(iconTypePage
                        .getRecords().stream()
                        .map(IconTypeDto::new)
                        .map(IconTypeDto::getVo)
                        .collect(Collectors.toList()))
                .setSize(iconTypePage.getSize())
                .setTotal(iconTypePage.getTotal());
    }

    /**
     * 新增分类
     *
     * @param iconTypeDto 图标分类VO
     * @return 新图标分类ID
     */
    @Override
    public Long addIconType(IconTypeDto iconTypeDto) {
        IconType iconType = iconTypeDto.getEntity()
                .setIsFinal(true);
        //临时id
//				.setTypeId(-1L);
        //TODO 异常处理，第一次出错隔3+random(5)值重试
        iconTypeMapper.insert(iconType);
//		iconTypeMapper.updateById(iconType.setTypeId(iconType.getId()));
        //设置父级
        if (!iconTypeDto.getParent().equals(-1L)) {
            iconTypeMapper.update(null, Wrappers.<IconType>lambdaUpdate()
                    .eq(IconType::getId, iconTypeDto.getParent())
                    .set(IconType::getIsFinal, false)
            );
        }
        //TODO 异常处理
        iconTypeMapper.insert(iconType);
        return iconType.getId();
    }

    /**
     * 修改分类
     *
     * @param iconTypeDto 图标分类VO
     * @return 是否成功
     */
    @Override
    public Boolean updateIconType(IconTypeDto iconTypeDto) {
        //获取图标分类实体
        IconType iconType = iconTypeMapper.selectOne(Wrappers.<IconType>lambdaQuery()
                .eq(IconType::getId, iconTypeDto.getTypeId()));
        //更改名称
        iconType.setName(iconTypeDto.getName());
        //判断是否是末端分类
        iconType.setIsFinal(
                iconTypeMapper.selectCount(Wrappers.<IconType>lambdaQuery()
                        .eq(IconType::getParent, iconTypeDto.getTypeId()))
                        > 0);
        //更改分类父级末端标志
        if (!iconTypeDto.getParent().equals(iconType.getParent())) {
            iconTypeMapper.update(null, Wrappers.<IconType>lambdaUpdate()
                    .eq(IconType::getId, iconTypeDto.getParent())
                    .set(IconType::getIsFinal, false)
            );
            //更改原父级的末端标志(如果原父级只剩这个子级的话)
            if (iconTypeMapper.selectCount(Wrappers.<IconType>lambdaQuery()
                    .eq(IconType::getParent, iconType.getParent()))
                    == 1) {
                iconTypeMapper.update(null, Wrappers.<IconType>lambdaUpdate()
                        .eq(IconType::getId, iconType.getParent())
                        .set(IconType::getIsFinal, true));
            }
            iconType.setParent(iconTypeDto.getParent());
        }
        //更新实体
        iconTypeMapper.updateById(iconType);
        return true;
    }

    /**
     * 删除分类，递归删除
     *
     * @param typeId 图标分类ID
     * @return 是否成功
     */
    @Override
    public Boolean deleteIconType(Long typeId) {
        //用于递归遍历删除的类型ID列表
        List<Long> nowTypeIdList = Collections.singletonList(typeId);
        while (!nowTypeIdList.isEmpty()) {
            //删除类型信息
            iconTypeMapper.delete(Wrappers.<IconType>lambdaQuery().in(IconType::getId, nowTypeIdList));
            //删除类型关联
            iconTypeLinkMapper.delete(Wrappers.<IconTypeLink>lambdaQuery().in(IconTypeLink::getTypeId, nowTypeIdList));
            //查找所有子级
            nowTypeIdList = iconTypeMapper.selectList(Wrappers.<IconType>lambdaQuery().in(IconType::getParent, nowTypeIdList))
                    .parallelStream()
                    .map(IconType::getId).collect(Collectors.toList());
        }
        return true;
    }
}
