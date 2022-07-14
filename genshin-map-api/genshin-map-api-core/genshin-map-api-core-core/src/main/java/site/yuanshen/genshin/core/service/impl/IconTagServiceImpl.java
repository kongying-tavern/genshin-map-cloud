package site.yuanshen.genshin.core.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import site.yuanshen.data.dto.TagDto;
import site.yuanshen.data.dto.TagSearchDto;
import site.yuanshen.data.dto.TagTypeDto;
import site.yuanshen.data.dto.helper.PageAndTypeListDto;
import site.yuanshen.data.entity.Icon;
import site.yuanshen.data.entity.Tag;
import site.yuanshen.data.entity.TagType;
import site.yuanshen.data.entity.TagTypeLink;
import site.yuanshen.data.mapper.IconMapper;
import site.yuanshen.data.mapper.TagMapper;
import site.yuanshen.data.mapper.TagTypeLinkMapper;
import site.yuanshen.data.mapper.TagTypeMapper;
import site.yuanshen.data.vo.TagTypeVo;
import site.yuanshen.data.vo.TagVo;
import site.yuanshen.data.vo.helper.PageListVo;
import site.yuanshen.genshin.core.service.IconTagService;
import site.yuanshen.genshin.core.service.mbp.TagMBPService;
import site.yuanshen.genshin.core.service.mbp.TagTypeLinkMBPService;
import site.yuanshen.genshin.core.service.mbp.TagTypeMBPService;

import java.util.*;
import java.util.stream.Collectors;

/**
 * TODO
 *
 * @author Moment
 */
@Service
@RequiredArgsConstructor
public class IconTagServiceImpl implements IconTagService {

    private final TagMapper tagMapper;
    private final TagMBPService tagMBPService;
    private final TagTypeMapper tagTypeMapper;
    private final TagTypeMBPService tagTypeMBPService;
    private final TagTypeLinkMapper tagTypeLinkMapper;
    private final TagTypeLinkMBPService tagTypeLinkMBPService;
    private final IconMapper iconMapper;

    /**
     * 列出标签
     *
     * @param tagSearchDto 图标标签分页查询VO
     * @return 图标标签前端对象列表
     */
    @Override
    public PageListVo<TagVo> listTag(TagSearchDto tagSearchDto) {
        //按照条件进行筛选
        Page<Tag> tagPage = tagMapper.selectPageIconTag(tagSearchDto.getPageEntity(), tagSearchDto);
        List<TagDto> tagDtoList = tagPage
                .getRecords()
                .stream().map(TagDto::new).collect(Collectors.toList());
        List<String> tagNameList = tagDtoList.stream()
                .map(TagDto::getTag).collect(Collectors.toList());
        //不存在tagList 则直接返回
        if (tagNameList.isEmpty()) {
            return new PageListVo<TagVo>()
                    .setRecord(new ArrayList<>())
                    .setTotal(tagPage.getTotal())
                    .setSize(tagPage.getSize());
        }

        //收集分类信息
        Map<String, List<Long>> typeMap = new HashMap<>();
        tagTypeLinkMapper.selectList(Wrappers.<TagTypeLink>lambdaQuery()
                        .in(TagTypeLink::getTagName, tagNameList))
                .forEach(typeLink -> {
                    List<Long> tempList = typeMap.getOrDefault(typeLink.getTagName(), new ArrayList<>());
                    tempList.add(typeLink.getTypeId());
                    typeMap.put(typeLink.getTagName(), tempList);
                });
        //收集图标信息
        List<Long> iconIdList = tagDtoList.stream().map(TagDto::getIconId).distinct().collect(Collectors.toList());
        Map<Long, String> urlMap = iconMapper.selectList(Wrappers.<Icon>lambdaQuery().in(Icon::getIconId, iconIdList))
                .stream().collect(Collectors.toMap(Icon::getIconId, Icon::getUrl));
        return new PageListVo<TagVo>()
                .setRecord(tagDtoList.stream().map(dto ->
                                dto.setTypeIdList(typeMap.getOrDefault(dto.getTag(), new ArrayList<>()))
                                        .setUrl(urlMap.getOrDefault(dto.getIconId(), ""))
                                        .getVo())
                        .collect(Collectors.toList()))
                .setTotal(tagPage.getTotal())
                .setSize(tagPage.getSize());
    }

    /**
     * 获取单个标签信息
     *
     * @param name 图标标签
     * @return 图标前端对象
     */
    @Override
    public TagDto getTag(String name) {
        //获取类型信息
        List<Long> typeIdList = tagTypeLinkMapper.selectList(Wrappers.<TagTypeLink>lambdaQuery()
                        .eq(TagTypeLink::getTagName, name)).stream()
                .map(TagTypeLink::getTypeId).collect(Collectors.toList());
        Tag tag = tagMapper.selectOne(Wrappers.<Tag>lambdaQuery()
                .eq(Tag::getTag, name));
        Icon icon = iconMapper.selectOne(Wrappers.<Icon>lambdaQuery().eq(Icon::getIconId, tag.getIconId()));
        return new TagDto(tag)
                .setTypeIdList(typeIdList)
                .setUrl(icon.getUrl());
    }

    /**
     * 修改标签关联
     *
     * @param tagName 标签名称
     * @param iconId  图标ID
     * @return 是否成功
     */
    @Override
    public Boolean updateTag(String tagName, Long iconId) {
        return tagMapper.update(null, Wrappers.<Tag>lambdaUpdate()
                .eq(Tag::getTag, tagName)
                .set(Tag::getIconId, iconId)) == 1;
    }

    /**
     * 修改标签的分类信息
     *
     * @param tagDto 标签Dto
     * @return 是否成功
     */
    @Override
    public Boolean updateTypeInTag(TagDto tagDto) {
        //删除旧类型链接
        tagTypeLinkMapper.delete(Wrappers.<TagTypeLink>lambdaQuery()
                .eq(TagTypeLink::getTagName, tagDto.getTag()));
        //检验并插入新类型
        List<Long> typeIdList = tagDto.getTypeIdList();
        if (typeIdList.size() != tagTypeMapper.selectList(Wrappers.<TagType>lambdaQuery().in(TagType::getId, typeIdList)).size())
            //TODO 异常处理
            throw new RuntimeException("类型ID错误");
        tagTypeLinkMBPService.saveBatch(
                typeIdList.stream()
                        .map(id -> new TagTypeLink().setTagName(tagDto.getTag()).setTypeId(id))
                        .collect(Collectors.toList())
        );
        return true;
    }

    /**
     * 创建标签，只创建一个空标签
     *
     * @param tagName 标签名称
     * @return 是否成功
     */
    @Override
    public Boolean createTag(String tagName) {
        return tagMapper.insert(new Tag().setTag(tagName)) == 1;
    }

    /**
     * 删除标签
     *
     * @param tagName 标签名称
     * @return 是否成功
     */
    @Override
    public Boolean deleteTag(String tagName) {
        tagTypeLinkMapper.delete(Wrappers.<TagTypeLink>lambdaQuery()
                .eq(TagTypeLink::getTagName, tagName));
        return tagMapper.delete(Wrappers.<Tag>lambdaQuery()
                .eq(Tag::getTag, tagName)) == 1;
    }

    /**
     * 列出分类
     *
     * @param searchDto 带遍历的分页查询VO
     * @return 图标标签分类列表
     */
    @Override
    public PageListVo<TagTypeVo> listTagType(PageAndTypeListDto searchDto) {
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
    public Long addTagType(TagTypeDto tagTypeDto) {
        TagType tagType = tagTypeDto.getEntity()
                .setIsFinal(true);
//				//临时id
//				.setTypeId(-1L);
        tagTypeMapper.insert(tagType);
//		tagTypeMapper.updateById(tagType.setTypeId(tagType.getId()));
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
