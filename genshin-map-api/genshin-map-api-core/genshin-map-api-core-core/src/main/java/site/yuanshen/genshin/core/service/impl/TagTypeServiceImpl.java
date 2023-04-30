package site.yuanshen.genshin.core.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import site.yuanshen.data.dto.TagTypeDto;
import site.yuanshen.data.dto.helper.PageAndTypeSearchDto;
import site.yuanshen.data.entity.TagType;
import site.yuanshen.data.entity.TagTypeLink;
import site.yuanshen.data.mapper.IconMapper;
import site.yuanshen.data.mapper.TagMapper;
import site.yuanshen.data.mapper.TagTypeLinkMapper;
import site.yuanshen.data.mapper.TagTypeMapper;
import site.yuanshen.data.vo.TagTypeVo;
import site.yuanshen.data.vo.helper.PageListVo;
import site.yuanshen.genshin.core.service.CacheService;
import site.yuanshen.genshin.core.service.TagTypeService;
import site.yuanshen.genshin.core.service.mbp.TagMBPService;
import site.yuanshen.genshin.core.service.mbp.TagTypeLinkMBPService;
import site.yuanshen.genshin.core.service.mbp.TagTypeMBPService;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 图标标签分类接口实现
 *
 * @author Alex Fang
 */
@Service
@RequiredArgsConstructor
public class TagTypeServiceImpl implements TagTypeService {

    private final CacheService cacheService;
    private final TagMapper tagMapper;
    private final TagMBPService tagMBPService;
    private final TagTypeMapper tagTypeMapper;
    private final TagTypeMBPService tagTypeMBPService;
    private final TagTypeLinkMapper tagTypeLinkMapper;
    private final TagTypeLinkMBPService tagTypeLinkMBPService;
    private final IconMapper iconMapper;

    /**
     * 列出分类
     *
     * @param searchDto 带遍历的分页查询VO
     * @return 图标标签分类列表
     */
    @Override
    @Cacheable(value = "listIconTagType")
    public PageListVo<TagTypeVo> listTagType(PageAndTypeSearchDto searchDto) {
        Page<TagType> tagTypePage = tagTypeMapper.selectPage(searchDto.getPageEntity(),
                Wrappers.<TagType>lambdaQuery()
                        .in(TagType::getParent,
                                Optional.ofNullable(searchDto.getTypeIdList())
                                        .orElse(Collections.singletonList(-1L)))
        );
        return new PageListVo<TagTypeVo>()
                .setRecord(tagTypePage.getRecords().stream()
                        .map(TagTypeDto::new)
                        .map(TagTypeDto::getVo)
                        .collect(Collectors.toList()))
                .setSize(tagTypePage.getSize())
                .setTotal(tagTypePage.getTotal());
    }

    /**
     * 新增分类
     *
     * @param tagTypeDto 图标标签分类VO
     * @return 新图标标签分类ID
     */
    @Override
    @Transactional
    @CacheEvict(value = "listIconTagType", allEntries = true)
    public Long addTagType(TagTypeDto tagTypeDto) {
        TagType tagType = tagTypeDto.getEntity()
                .withIsFinal(true);
        tagTypeMapper.insert(tagType);
        //设置父级
        if (!tagTypeDto.getParent().equals(-1L)) {
            tagTypeMapper.update(null, Wrappers.<TagType>lambdaUpdate()
                    .eq(TagType::getId, tagTypeDto.getParent())
                    .set(TagType::getIsFinal, false));
        }
        return tagType.getId();
    }

    /**
     * 修改分类
     *
     * @param tagTypeDto 图标标签分类VO
     * @return 是否成功
     */
    @Override
    @Transactional
    @CacheEvict(value = "listIconTagType", allEntries = true)
    public Boolean updateTagType(TagTypeDto tagTypeDto) {
        //获取标签分类实体
        TagType tagType = tagTypeMapper.selectOne(Wrappers.<TagType>lambdaQuery()
                .eq(TagType::getId, tagTypeDto.getId()));
        //更改名称
        tagType.setName(tagTypeDto.getName());
        //判断是否是末端分类
        tagType.setIsFinal(
                tagTypeMapper.selectOne(Wrappers.<TagType>lambdaQuery()
                        .eq(TagType::getParent, tagTypeDto.getId()))
                        == null);
        //更改分类父级
        if (!tagTypeDto.getParent().equals(tagType.getParent())) {
            tagTypeMapper.update(null, Wrappers.<TagType>lambdaUpdate()
                    .eq(TagType::getId, tagTypeDto.getParent())
                    .set(TagType::getIsFinal, false));
            //更改原父级的末端标志(如果原父级只剩这个子级的话)
            if (tagTypeMapper.selectCount(Wrappers.<TagType>lambdaQuery()
                    .eq(TagType::getParent, tagType.getParent()))
                    == 1) {
                tagTypeMapper.update(null, Wrappers.<TagType>lambdaUpdate()
                        .eq(TagType::getId, tagType.getParent())
                        .set(TagType::getIsFinal, true));
            }
            tagType.setParent(tagTypeDto.getParent());
        }
        //更新实体
        tagTypeMapper.updateById(tagType);
        return true;
    }

    /**
     * 删除分类，递归删除
     *
     * @param typeId 图标标签分类ID
     * @return 是否成功
     */
    @Override
    @Transactional
    @CacheEvict(value = "listIconTagType", allEntries = true)
    public Boolean deleteTagType(Long typeId) {
        //用于递归遍历删除的类型ID列表
        List<Long> nowTypeIdList = Collections.singletonList(typeId);
        while (!nowTypeIdList.isEmpty()) {
            //删除类型信息
            tagTypeMapper.delete(Wrappers.<TagType>lambdaQuery().in(TagType::getId, nowTypeIdList));
            //删除类型关联
            tagTypeLinkMapper.delete(Wrappers.<TagTypeLink>lambdaQuery().in(TagTypeLink::getTypeId, nowTypeIdList));
            //查找所有子级
            nowTypeIdList = tagTypeMapper.selectList(Wrappers.<TagType>lambdaQuery().in(TagType::getParent, nowTypeIdList))
                    .parallelStream()
                    .map(TagType::getId).collect(Collectors.toList());
        }
        return true;
    }
}
