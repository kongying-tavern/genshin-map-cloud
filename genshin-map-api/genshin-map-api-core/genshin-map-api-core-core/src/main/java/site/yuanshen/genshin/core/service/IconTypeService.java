package site.yuanshen.genshin.core.service;

import cn.hutool.core.util.ObjUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import site.yuanshen.common.core.exception.GenshinApiException;
import site.yuanshen.common.core.utils.BeanUtils;
import site.yuanshen.data.dto.IconTypeDto;
import site.yuanshen.data.dto.helper.PageAndTypeSearchDto;
import site.yuanshen.data.entity.IconType;
import site.yuanshen.data.entity.IconTypeLink;
import site.yuanshen.data.mapper.IconTypeLinkMapper;
import site.yuanshen.data.mapper.IconTypeMapper;
import site.yuanshen.data.vo.IconTypeVo;
import site.yuanshen.data.vo.helper.PageListVo;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 图标服务接口实现
 *
 * @author Moment
 */
@Service
@RequiredArgsConstructor
public class IconTypeService {
    private final IconTypeMapper iconTypeMapper;
    private final IconTypeLinkMapper iconTypeLinkMapper;

    /**
     * 列出分类
     *
     * @param searchDto 带遍历的分页查询DTO
     * @return 图标类型列表
     */
    @Cacheable(value = "listIconType")
    public PageListVo<IconTypeVo> listIconType(PageAndTypeSearchDto searchDto) {
        Page<IconType> iconTypePage = iconTypeMapper.selectPage(searchDto.getPageEntity(),
                Wrappers.<IconType>lambdaQuery()
                        .in(IconType::getParentId,
                                Optional.ofNullable(searchDto.getTypeIdList())
                                        .orElse(Collections.singletonList(-1L))));
        List<IconTypeVo> result = iconTypePage
                .getRecords().stream()
                .map(IconTypeDto::new)
                .map(IconTypeDto::getVo)
                .collect(Collectors.toList());
        return new PageListVo<IconTypeVo>()
                .setRecord(result)
                .setSize(iconTypePage.getSize())
                .setTotal(iconTypePage.getTotal());
    }

    /**
     * 新增分类
     *
     * @param iconTypeDto 图标分类VO
     * @return 新图标分类ID
     */
    @Transactional
    @Caching(
            evict = {
                    @CacheEvict(value = "listIcon", allEntries = true),
                    @CacheEvict(value = "listIconType", allEntries = true)
            }
    )
    public Long addIconType(IconTypeDto iconTypeDto) {
        if (Objects.equals(iconTypeDto.getId(), iconTypeDto.getParentId())) {
            throw new GenshinApiException("图标类型ID不允许与父ID相同，会造成自身父子");
        }

        IconType iconType = iconTypeDto.getEntity()
            .withIsFinal(true);
        iconTypeMapper.insert(iconType);

        //更新父级的末端标志
        updateIconTypeIsFinal(iconTypeDto.getParentId(), false);

        return iconType.getId();
    }

    /**
     * 修改分类
     *
     * @param iconTypeDto 图标分类VO
     * @return 是否成功
     */
    @Transactional
    @Caching(
            evict = {
                    @CacheEvict(value = "icon", allEntries = true),
                    @CacheEvict(value = "listIcon", allEntries = true),
                    @CacheEvict(value = "listIconType", allEntries = true)
            }
    )
    public Boolean updateIconType(IconTypeDto iconTypeDto) {
        if (Objects.equals(iconTypeDto.getId(), iconTypeDto.getParentId())) {
            throw new GenshinApiException("图标类型ID不允许与父ID相同，会造成自身父子");
        }

        //获取图标分类实体
        IconType iconType = iconTypeMapper.selectOne(Wrappers.<IconType>lambdaQuery()
                .eq(IconType::getId, iconTypeDto.getId()));
        if(ObjUtil.isNull(iconType)){
            return false;
        }

        //更新父级的末端标志
        if (!Objects.equals(iconType.getParentId(), iconTypeDto.getParentId())) {
            // 更改新父级的末端标识
            updateIconTypeIsFinal(iconTypeDto.getParentId(), false);
            //更改原父级的末端标志(如果原父级只剩这个子级的话)
            recalculateIconTypeIsFinal(iconType.getParentId(), true);
        }
        //更新实体
        BeanUtils.copyNotNull(iconTypeDto.getEntity(), iconType);
        updateIconTypeIsFinal(iconType);
        iconTypeMapper.updateById(iconType);
        return true;
    }

    /**
     * 删除分类，递归删除
     *
     * @param iconTypeId 图标分类ID
     * @return 是否成功
     */
    @Transactional
    @Caching(
            evict = {
                    @CacheEvict(value = "icon", allEntries = true),
                    @CacheEvict(value = "listIcon", allEntries = true),
                    @CacheEvict(value = "listIconType", allEntries = true)
            }
    )
    public Boolean deleteIconType(Long iconTypeId) {
        final Long parentIconTypeId = iconTypeMapper.selectById(iconTypeId).getParentId();

        List<Long> nowTypeIdList = Collections.singletonList(iconTypeId);
        while (!nowTypeIdList.isEmpty()) {
            //删除类型信息
            iconTypeMapper.delete(Wrappers.<IconType>lambdaQuery().in(IconType::getId, nowTypeIdList));
            //删除类型关联
            iconTypeLinkMapper.delete(Wrappers.<IconTypeLink>lambdaQuery().in(IconTypeLink::getTypeId, nowTypeIdList));
            //查找所有子级
            nowTypeIdList = iconTypeMapper.selectList(Wrappers.<IconType>lambdaQuery().in(IconType::getParentId, nowTypeIdList))
                    .parallelStream()
                    .map(IconType::getId).collect(Collectors.toList());
        }

        //更新父级标记
        recalculateIconTypeIsFinal(parentIconTypeId, false);

        return true;
    }

    private void updateIconTypeIsFinal(Long parentId, boolean isFinal) {
            if(parentId != null && parentId > 0L) {
            iconTypeMapper.update(null, Wrappers.<IconType>lambdaUpdate()
                .eq(IconType::getId, parentId)
                .set(IconType::getIsFinal, isFinal));
        }
    }

    private void updateIconTypeIsFinal(IconType iconType) {
        if(iconType != null) {
            iconType.setIsFinal(iconTypeMapper.selectCount(Wrappers.<IconType>lambdaQuery()
                .eq(IconType::getParentId, iconType.getId()))
                == 0);
        }
    }

    private void recalculateIconTypeIsFinal(Long parentId, boolean beforeModify) {
        if(parentId != null) {
            if (
                iconTypeMapper.selectCount(Wrappers.<IconType>lambdaQuery()
                    .eq(IconType::getParentId, parentId))
                    == (beforeModify ? 1 : 0)
            ) {
                iconTypeMapper.update(null, Wrappers.<IconType>lambdaUpdate()
                        .eq(IconType::getId, parentId)
                        .set(IconType::getIsFinal, true));
            } else {
                iconTypeMapper.update(null, Wrappers.<IconType>lambdaUpdate()
                        .eq(IconType::getId, parentId)
                        .set(IconType::getIsFinal, false));
            }
        }
    }
}
