package site.yuanshen.genshin.core.service;

import site.yuanshen.data.dto.IconDto;
import site.yuanshen.data.dto.IconSearchDto;
import site.yuanshen.data.dto.IconTypeDto;
import site.yuanshen.data.dto.helper.PageAndTypeListDto;
import site.yuanshen.data.vo.IconTypeVo;
import site.yuanshen.data.vo.IconVo;
import site.yuanshen.data.vo.helper.PageListVo;

/**
 * 图标服务接口
 *
 * @author Moment
 */
public interface IconService {

    //////////////START:图标本身的接口//////////////

    /**
     * 列出图标
     *
     * @param iconSearchDto 图标分页查询Dto
     * @return 图标前端对象列表
     */
    PageListVo<IconVo> listIcon(IconSearchDto iconSearchDto);

    /**
     * 获取单个图标信息
     *
     * @param iconId 图标ID
     * @return 图标前端对象
     */
    IconDto getIcon(Long iconId);

    /**
     * 修改图标信息
     *
     * @param iconDto 图标前端对象
     * @return 是否成功
     */
    Boolean updateIcon(IconDto iconDto);

    /**
     * 新增图标
     *
     * @param iconDto 图标前端对象
     * @return 新图标的ID
     */
    Long createIcon(IconDto iconDto);

    /**
     * 删除图标
     *
     * @param iconId 图标ID
     * @return 是否成功
     */
    Boolean deleteIcon(Long iconId);

    //////////////END:图标本身的接口//////////////

    //////////////START:图标分类的接口//////////////

    /**
     * 列出分类
     *
     * @param searchDto 带遍历的分页查询Dto
     * @return 图标类型列表
     */
    PageListVo<IconTypeVo> listIconType(PageAndTypeListDto searchDto);

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
