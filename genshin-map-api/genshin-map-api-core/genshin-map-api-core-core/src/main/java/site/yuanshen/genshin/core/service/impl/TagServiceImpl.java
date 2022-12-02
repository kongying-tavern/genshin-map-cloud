package site.yuanshen.genshin.core.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import site.yuanshen.data.dto.TagDto;
import site.yuanshen.data.dto.TagSearchDto;
import site.yuanshen.data.entity.Icon;
import site.yuanshen.data.entity.Tag;
import site.yuanshen.data.entity.TagType;
import site.yuanshen.data.entity.TagTypeLink;
import site.yuanshen.data.mapper.IconMapper;
import site.yuanshen.data.mapper.TagMapper;
import site.yuanshen.data.mapper.TagTypeLinkMapper;
import site.yuanshen.data.mapper.TagTypeMapper;
import site.yuanshen.data.vo.TagVo;
import site.yuanshen.data.vo.helper.PageListVo;
import site.yuanshen.genshin.core.service.CacheService;
import site.yuanshen.genshin.core.service.TagService;
import site.yuanshen.genshin.core.service.mbp.TagMBPService;
import site.yuanshen.genshin.core.service.mbp.TagTypeLinkMBPService;
import site.yuanshen.genshin.core.service.mbp.TagTypeMBPService;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * TODO
 *
 * @author Moment
 */
@Service
@RequiredArgsConstructor
public class TagServiceImpl implements TagService {

    private final CacheService cacheService;
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
    @Cacheable(value = "listIconTag")
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
    @Cacheable(value = "iconTag", key = "#name")
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
    @Transactional
    public Boolean updateTag(String tagName, Long iconId) {
        boolean isUpdate = tagMapper.update(null, Wrappers.<Tag>lambdaUpdate()
                .eq(Tag::getTag, tagName)
                .set(Tag::getIconId, iconId)) == 1;
        if (!isUpdate) throw new RuntimeException("未进行实质修改");
        return true;
    }

    /**
     * 修改标签的分类信息
     *
     * @param tagDto 标签Dto
     * @return 是否成功
     */
    @Override
    @Transactional
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
    @Transactional
    public Boolean createTag(String tagName) {
        //判断是否重复
        Tag tag = tagMapper.selectOne(Wrappers.<Tag>lambdaQuery().eq(Tag::getTag, tagName));
        if (tag == null) {
            return tagMapper.insert(new Tag().setTag(tagName)) == 1;
        } else {
            return false;
        }
    }

    /**
     * 删除标签
     *
     * @param tagName 标签名称
     * @return 是否成功
     */
    @Override
    @Transactional
    public Boolean deleteTag(String tagName) {
        tagTypeLinkMapper.delete(Wrappers.<TagTypeLink>lambdaQuery()
                .eq(TagTypeLink::getTagName, tagName));
        if (tagMapper.delete(Wrappers.<Tag>lambdaQuery()
                .eq(Tag::getTag, tagName)) != 1) {
            throw new RuntimeException("无删除的标签");
        }
        return true;
    }
}