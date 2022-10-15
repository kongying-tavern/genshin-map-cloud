package site.yuanshen.genshin.core.service;

import site.yuanshen.data.dto.helper.PageSearchDto;
import site.yuanshen.data.vo.ItemVo;
import site.yuanshen.data.vo.helper.PageListVo;

import java.util.List;

/**
 * 公用物品服务接口
 *
 * @author Alex Fang
 */
public interface ItemCommonService {

    //////////////START:地区公用物品的接口//////////////

    /**
     * 列出地区公用物品
     *
     * @param pageSearchDto 分页查询数据封装
     * @return 物品前端封装的分页封装
     */
    PageListVo<ItemVo> listCommonItem(PageSearchDto pageSearchDto);

    /**
     * 新增地区公用物品
     *
     * @param itemIdList 物品ID列表
     * @return 是否成功
     */
    Boolean addCommonItem(List<Long> itemIdList);

    /**
     * 删除地区公用物品
     *
     * @param itemId 物品ID
     * @return 是否成功
     */
    Boolean deleteCommonItem(Long itemId);

    //////////////END:地区公用物品的接口//////////////

}
