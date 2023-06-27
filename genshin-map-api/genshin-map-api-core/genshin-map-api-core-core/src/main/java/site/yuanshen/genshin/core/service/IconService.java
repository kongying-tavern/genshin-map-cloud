package site.yuanshen.genshin.core.service;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import site.yuanshen.data.dto.IconDto;
import site.yuanshen.data.dto.IconSearchDto;
import site.yuanshen.data.entity.Icon;
import site.yuanshen.data.entity.IconType;
import site.yuanshen.data.entity.IconTypeLink;
import site.yuanshen.data.mapper.IconMapper;
import site.yuanshen.data.mapper.IconTypeLinkMapper;
import site.yuanshen.data.mapper.IconTypeMapper;
import site.yuanshen.data.vo.IconVo;
import site.yuanshen.data.vo.helper.PageListVo;
import site.yuanshen.genshin.core.service.mbp.IconTypeLinkMBPService;

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
public class IconService {

    private final IconMapper iconMapper;
    private final IconTypeMapper iconTypeMapper;
    private final IconTypeLinkMapper iconTypeLinkMapper;
    private final IconTypeLinkMBPService iconTypeLinkMBPService;

    /**
     * 列出图标
     *
     * @param searchDto 图标分页查询VO
     * @return 图标前端对象列表
     */
    @Cacheable(value = "listIcon")
    public PageListVo<IconVo> listIcon(IconSearchDto searchDto) {
        Page<Icon> iconPage = iconMapper.selectPageIcon(searchDto.getPageEntity(), searchDto);
        //按照条件进行筛选
        List<IconDto> iconDtoList = iconPage
                .getRecords()
                .stream()
                .map(IconDto::new)
                .collect(Collectors.toList());
        if(iconDtoList.isEmpty()){
            return new PageListVo<IconVo>().setRecord(new ArrayList<>()).setSize(iconPage.getSize()).setTotal(iconPage.getTotal());
        }
        //收集分类信息
        Map<Long, List<Long>> typeMap = new ConcurrentHashMap<>();
        iconTypeLinkMapper.selectList(Wrappers.<IconTypeLink>lambdaQuery()
                        .in(IconTypeLink::getId, iconDtoList.parallelStream()
                                .map(IconDto::getId).collect(Collectors.toList())))
                //TODO 验证此处并行是否会有bug
                .parallelStream()
                .forEach(typeLink ->
                        typeMap.compute(typeLink.getId(), (iconId, typeList) -> {
                            if (typeList == null) return new ArrayList<>(Collections.singletonList(typeLink.getTypeId()));
                            typeList.add(typeLink.getTypeId());
                            return typeList;
                        }));
        //写入分类信息
        return new PageListVo<IconVo>()
                .setRecord(iconDtoList.stream().map(dto ->
                                dto.getVo().withTypeIdList(typeMap.getOrDefault(dto.getId(), new ArrayList<>())))
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
    @Cacheable(value = "icon", key = "#iconId")
    public IconVo getIcon(Long iconId) {
        //获取类型信息
        List<Long> typeIdList = iconTypeLinkMapper.selectList(Wrappers.<IconTypeLink>lambdaQuery()
                        .eq(IconTypeLink::getId, iconId)).stream()
                .map(IconTypeLink::getTypeId).collect(Collectors.toList());
        return new IconDto(
                iconMapper.selectOne(Wrappers.<Icon>lambdaQuery()
                        .eq(Icon::getId, iconId))
        ).getVo().withTypeIdList(typeIdList);
    }

    /**
     * 修改图标信息
     *
     * @param iconVo 图标前端对象
     * @return 是否成功
     */
    @Transactional
    @Caching(
            evict = {
                    @CacheEvict(value = "icon", key = "#iconVo.id"),
                    @CacheEvict(value = "listIcon", allEntries = true)
            }
    )
    public Boolean updateIcon(IconVo iconVo) {
        IconDto iconDto = new IconDto(iconVo);
        //取类型ID
        HashSet<Long> newTypeIds = new HashSet<>(iconVo.getTypeIdList());
        //对比类型信息是否更改
        HashSet<Long> oldTypeIds = iconTypeLinkMapper.selectList(Wrappers.<IconTypeLink>lambdaQuery()
                        .eq(IconTypeLink::getId, iconDto.getId()))
                .stream()
                .map(IconTypeLink::getTypeId).collect(Collectors.toCollection(HashSet::new));
        //如果类型ID更改了就进行分类的刷新
        if (!oldTypeIds.equals(newTypeIds)) {
            iconTypeLinkMapper.delete(Wrappers.<IconTypeLink>lambdaQuery()
                    .eq(IconTypeLink::getId, iconDto.getId()));
            iconTypeLinkMBPService.saveBatch(
                    newTypeIds.stream()
                    .map(id -> new IconTypeLink()
                            .withIconId(iconDto.getId())
                            .withId(id))
                            .collect(Collectors.toList())
            );
        }
        //更新实体信息
        return iconMapper.update(iconDto.getEntity(),
                Wrappers.<Icon>lambdaUpdate()
                        .eq(Icon::getId, iconVo.getId()))
                == 1;
    }

    /**
     * 新增图标
     *
     * @param iconVo 图标前端对象
     * @return 新图标的ID
     */
    @Transactional
    @CacheEvict(value = "listIcon", allEntries = true)
    public Long createIcon(IconVo iconVo) {
        IconDto iconDto = new IconDto(iconVo);
        //取类型信息
        List<Long> typeIdList = iconVo.getTypeIdList();
        Icon icon = iconDto.getEntity()
                //临时id
                .withId(-1L);
        iconMapper.insert(icon);
        //正式更新id
        iconMapper.updateById(icon.withId(icon.getId()));
        //处理类型信息
        if (typeIdList != null) {
            //判断是否有不存在的类型ID
            if (typeIdList.size() != iconTypeMapper.selectCount(Wrappers.<IconType>lambdaQuery().in(IconType::getId, typeIdList)))
                throw new RuntimeException("类型ID错误");
            iconTypeLinkMBPService.saveBatch(
                    typeIdList.stream()
                            .map(id -> new IconTypeLink().withId(id).withIconId(icon.getId()))
                            .collect(Collectors.toList())
            );
        }
        return icon.getId();
    }

    /**
     * 删除图标
     *
     * @param iconId 图标ID
     * @return 是否成功
     */
    @Transactional
    @Caching(
            evict = {
                    @CacheEvict(value = "icon", key = "#iconId"),
                    @CacheEvict(value = "listIcon", allEntries = true)
            }
    )
    public Boolean deleteIcon(Long iconId) {
        iconTypeLinkMapper.delete(Wrappers.<IconTypeLink>lambdaQuery()
                .eq(IconTypeLink::getId, iconId));
        return iconMapper.delete(Wrappers.<Icon>lambdaQuery()
                .eq(Icon::getId, iconId))
                == 1;
    }
}
