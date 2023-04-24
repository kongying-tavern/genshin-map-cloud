package site.yuanshen.genshin.core.service;

import site.yuanshen.data.dto.IconSearchDto;
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
    IconVo getIcon(Long iconId);

    /**
     * 修改图标信息
     *
     * @param iconVo 图标前端对象
     * @return 是否成功
     */
    Boolean updateIcon(IconVo iconVo);

    /**
     * 新增图标
     *
     * @param iconVo 图标前端对象
     * @return 新图标的ID
     */
    Long createIcon(IconVo iconVo);

    /**
     * 删除图标
     *
     * @param iconId 图标ID
     * @return 是否成功
     */
    Boolean deleteIcon(Long iconId);

    //////////////END:图标本身的接口//////////////

}
