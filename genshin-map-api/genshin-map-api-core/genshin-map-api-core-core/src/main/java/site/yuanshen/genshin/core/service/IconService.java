package site.yuanshen.genshin.core.service;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import site.yuanshen.common.core.exception.GenshinApiException;
import site.yuanshen.common.core.utils.JsonUtils;
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
        if (iconDtoList.isEmpty()) {
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
        List<IconVo> result = iconDtoList.stream().map(dto ->
                dto.getVo().withTypeIdList(typeMap.getOrDefault(dto.getId(), new ArrayList<>())))
            .collect(Collectors.toList());
        return new PageListVo<IconVo>()
            .setRecord(result)
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
        Icon iconEntity = iconMapper.selectOne(Wrappers.<Icon>lambdaQuery()
            .eq(Icon::getId, iconId));
        return iconEntity == null ? null : new IconDto(iconEntity).getVo().withTypeIdList(typeIdList);
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
        // 同名标签处理
        final String tagName = StrUtil.blankToDefault(iconVo.getTag(), "");
        if (StrUtil.isBlank(tagName)) {
            throw new GenshinApiException("图标标签不能为空");
        }
        final Icon validateIcon = iconMapper.getIcon(tagName, false);
        if (validateIcon != null && !Objects.equals(validateIcon.getId(), iconVo.getId())) {
            throw new GenshinApiException("图标标签 [" + tagName + "] 已存在");
        }

        IconDto iconRecord = new IconDto(iconMapper.selectOne(Wrappers.<Icon>lambdaQuery()
            .eq(Icon::getId, iconVo.getId())
        ));
        IconDto iconDto = new IconDto(iconVo);

        // 合并 URL 变体设置
        Map<String, String> mergedUrlVariants = JsonUtils.merge(iconRecord.getUrlVariants(), iconDto.getUrlVariants());
        iconDto.setUrlVariants(mergedUrlVariants);

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
                    .map(typeId -> new IconTypeLink()
                        .withIconId(iconDto.getId())
                        .withTypeId(typeId))
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
        // 同名标签处理
        final String tagName = StrUtil.blankToDefault(iconVo.getTag(), "");
        if (StrUtil.isBlank(tagName)) {
            throw new GenshinApiException("图标标签不能为空");
        }
        final Icon validateIcon = iconMapper.getIcon(tagName, true);
        if (validateIcon != null) {
            throw new GenshinApiException("图标标签 [" + tagName + "]已存在");
        }

        IconDto iconDto = new IconDto(iconVo);

        // 合并 URL 变体设置
        Map<String, String> mergedUrlVariants = JsonUtils.merge(new HashMap<>(), iconDto.getUrlVariants());
        iconDto.setUrlVariants(mergedUrlVariants);

        //取类型信息
        List<Long> typeIdList = iconVo.getTypeIdList();
        Icon icon = iconDto.getEntity();
        iconMapper.insert(icon);
        //处理类型信息
        if (CollectionUtil.isNotEmpty(typeIdList)) {
            //判断是否有不存在的类型ID
            if (typeIdList.size() != iconTypeMapper.selectCount(Wrappers.<IconType>lambdaQuery().in(IconType::getId, typeIdList)))
                throw new GenshinApiException("类型ID错误");
            iconTypeLinkMBPService.saveBatch(
                typeIdList.stream()
                    .map(typeId -> new IconTypeLink().withTypeId(typeId).withIconId(icon.getId()))
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
