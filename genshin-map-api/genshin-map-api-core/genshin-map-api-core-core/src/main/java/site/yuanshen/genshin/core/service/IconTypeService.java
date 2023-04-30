package site.yuanshen.genshin.core.service;

import site.yuanshen.data.dto.IconTypeDto;
import site.yuanshen.data.dto.helper.PageAndTypeSearchDto;
import site.yuanshen.data.vo.IconTypeVo;
import site.yuanshen.data.vo.helper.PageListVo;

/**
 * 图标分类服务接口
 *
 * @author Alex Fang
 */
public interface IconTypeService {

    //////////////START:图标分类的接口//////////////

    /**
     * 列出分类
     *
     * @param searchDto 带遍历的分页查询Dto
     * @return 图标类型列表
     */
    PageListVo<IconTypeVo> listIconType(PageAndTypeSearchDto searchDto);

    /**
     * 新增分类
     *
     * @param iconTypeDto 图标分类Dto
     * @return 新图标分类ID
     */
    Long addIconType(IconTypeDto iconTypeDto);

    /**
     * 修改分类
     *
     * @param iconTypeDto 图标分类Dto
     * @return 是否成功
     */
    Boolean updateIconType(IconTypeDto iconTypeDto);

    /**
     * 删除分类，递归删除
     *
     * @param typeId 图标分类ID
     * @return 是否成功
     */
    Boolean deleteIconType(Long typeId);

    //////////////END:图标分类的接口//////////////

}
