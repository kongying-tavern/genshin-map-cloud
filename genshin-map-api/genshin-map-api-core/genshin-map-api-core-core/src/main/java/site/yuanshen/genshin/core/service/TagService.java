package site.yuanshen.genshin.core.service;

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
 * 图标标签服务接口实现
 *
 * @author Moment
 */
@Service
@RequiredArgsConstructor
public class TagService {

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
        List<Long> iconIdList = tagDtoList.stream().map(TagDto::getId).distinct().collect(Collectors.toList());
        Map<Long, String> urlMap = iconMapper.selectList(Wrappers.<Icon>lambdaQuery().in(Icon::getId, iconIdList))
                .stream().collect(Collectors.toMap(Icon::getId, Icon::getUrl));
        PageListVo<TagVo> page = new PageListVo<TagVo>()
                .setRecord(tagDtoList.stream().map(dto ->
                                dto.getVo().withTypeIdList(typeMap.getOrDefault(dto.getTag(), new ArrayList<>()))
                                        .withUrl(urlMap.getOrDefault(dto.getId(), "")))
                        .collect(Collectors.toList()))
                .setTotal(tagPage.getTotal())
                .setSize(tagPage.getSize());
        return page;
    }

    /**
     * 获取单个标签信息
     *
     * @param name 图标标签
     * @return 图标前端对象
     */
    @Cacheable(value = "iconTag", key = "#name")
    public TagVo getTag(String name) {
        //获取类型信息
        List<Long> typeIdList = tagTypeLinkMapper.selectList(Wrappers.<TagTypeLink>lambdaQuery()
                        .eq(TagTypeLink::getTagName, name)).stream()
                .map(TagTypeLink::getTypeId).collect(Collectors.toList());
        Tag tag = tagMapper.selectOne(Wrappers.<Tag>lambdaQuery()
                .eq(Tag::getTag, name));
        Icon icon = iconMapper.selectOne(Wrappers.<Icon>lambdaQuery().eq(Icon::getId, tag.getId()));
        TagVo result = new TagDto(tag)
                .getVo()
                .withTypeIdList(typeIdList)
                .withUrl(icon.getUrl());
        return result;
    }

    /**
     * 修改标签关联
     *
     * @param tagName 标签名称
     * @param iconId  图标ID
     * @return 是否成功
     */
    @Transactional
    public Boolean updateTag(String tagName, Long iconId) {
        boolean isUpdate = tagMapper.update(null, Wrappers.<Tag>lambdaUpdate()
                .eq(Tag::getTag, tagName)
                .set(Tag::getIconId, iconId)) == 1;
        if (!isUpdate) throw new RuntimeException("与原数据一致，未进行实质修改");
        return true;
    }

    /**
     * 修改标签的分类信息
     *
     * @param tagVo 标签Dto
     * @return 是否成功
     */
    @Transactional
    public Boolean updateTypeInTag(TagVo tagVo) {
        TagDto tagDto = new TagDto(tagVo);
        List<Long> typeIdList = tagVo.getTypeIdList();
        //删除旧类型链接
        tagTypeLinkMapper.delete(Wrappers.<TagTypeLink>lambdaQuery()
                .eq(TagTypeLink::getTagName, tagDto.getTag()));
        //检验并插入新类型
        if (typeIdList.size() != tagTypeMapper.selectList(Wrappers.<TagType>lambdaQuery().in(TagType::getId, typeIdList)).size())
            throw new RuntimeException("类型ID错误");
        tagTypeLinkMBPService.saveBatch(
                typeIdList.stream()
                        .map(id -> new TagTypeLink().withTagName(tagVo.getTag()).withTypeId(id))
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
    @Transactional
    public Boolean createTag(String tagName) {
        //判断是否重复
        Tag tag = tagMapper.selectOne(Wrappers.<Tag>lambdaQuery().eq(Tag::getTag, tagName));
        if (tag == null) {
            return tagMapper.insert(new Tag().withTag(tagName)) == 1;
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
