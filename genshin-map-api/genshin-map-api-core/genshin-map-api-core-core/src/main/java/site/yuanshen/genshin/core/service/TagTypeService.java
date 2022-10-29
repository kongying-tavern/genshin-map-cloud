package site.yuanshen.genshin.core.service;

import site.yuanshen.data.dto.TagTypeDto;
import site.yuanshen.data.dto.helper.PageAndTypeListDto;
import site.yuanshen.data.vo.TagTypeVo;
import site.yuanshen.data.vo.helper.PageListVo;

/**
 * 图标标签分类服务接口
 *
 * @author Alex Fang
 */
public interface TagTypeService {
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
