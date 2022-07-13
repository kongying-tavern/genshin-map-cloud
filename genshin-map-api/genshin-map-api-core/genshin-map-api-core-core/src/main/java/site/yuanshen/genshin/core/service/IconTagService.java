package site.yuanshen.genshin.core.service;

import site.yuanshen.data.dto.TagDto;
import site.yuanshen.data.dto.TagSearchDto;
import site.yuanshen.data.dto.TagTypeDto;
import site.yuanshen.data.dto.helper.PageAndTypeListDto;
import site.yuanshen.data.vo.TagTypeVo;
import site.yuanshen.data.vo.TagVo;
import site.yuanshen.data.vo.helper.PageListVo;

/**
 * 图标标签服务接口
 *
 * @author Moment
 */
public interface IconTagService {

    //////////////START:标签本身的接口//////////////

    /**
     * 列出标签
     *
     * @param tagSearchDto 图标标签分页查询Dto
     * @return 图标标签前端对象列表
     */
    PageListVo<TagVo> listTag(TagSearchDto tagSearchDto);

    /**
     * 获取单个标签信息
     *
     * @param name 图标标签
     * @return 图标前端对象
     */
    TagDto getTag(String name);

    /**
     * 修改标签关联
     *
     * @param tagName 标签名称
     * @param iconId  图标ID
     * @return 是否成功
     */
    Boolean updateTag(String tagName, Long iconId);

    /**
     * 修改标签的分类信息
     *
     * @param tagDto 标签Dto
     * @return 是否成功
     */
    Boolean updateTypeInTag(TagDto tagDto);

    /**
     * 创建标签，只创建一个空标签
     *
     * @param tagName 标签名称
     * @return 是否成功
     */
    Boolean createTag(String tagName);

    /**
     * 删除标签
     *
     * @param tagName 标签名称
     * @return 是否成功
     */
    Boolean deleteTag(String tagName);

    //////////////END:标签本身的接口//////////////

    //////////////START:标签分类的接口//////////////

    /**
     * 列出分类
     *
     * @param searchDto 带遍历的分页查询DTO
     * @return 图标标签分类列表
     */
    PageListVo<TagTypeVo> listTagType(PageAndTypeListDto searchDto);

    /**
     * 新增分类
     *
     * @param tagTypeDto 图标标签分类DTO
     * @return 新图标标签分类ID
     */
    Long addTagType(TagTypeDto tagTypeDto);

    /**
     * 修改分类
     *
     * @param tagTypeDto 图标标签分类DTO
     * @return 是否成功
     */
    Boolean updateTagType(TagTypeDto tagTypeDto);

    /**
     * 删除分类，递归删除
     *
     * @param typeId 图标标签分类ID
     * @return 是否成功
     */
    Boolean deleteTagType(Long typeId);

    //////////////END:标签分类的接口//////////////

}
