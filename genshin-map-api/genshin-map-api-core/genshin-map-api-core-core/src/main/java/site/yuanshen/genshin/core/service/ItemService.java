package site.yuanshen.genshin.core.service;

import site.yuanshen.data.dto.ItemDto;
import site.yuanshen.data.dto.ItemSearchDto;
import site.yuanshen.data.vo.ItemVo;
import site.yuanshen.data.vo.helper.PageListVo;

import java.util.List;

/**
 * 物品服务接口
 *
 * @author Moment
 */
public interface ItemService {

    //////////////START:物品本身的接口//////////////

    /**
     * 根据物品ID查询物品
     *
     * @param itemIdList 物品ID列表
     * @return 物品数据封装列表
     */
    List<ItemDto> listItemById(List<Long> itemIdList,List<Integer> hiddenFlagList);

    /**
     * 根据筛选条件列出物品信息
     *
     * @param itemSearchDto 物品查询前端封装
     * @return 物品ID列表
     */
    PageListVo<ItemVo> listItem(ItemSearchDto itemSearchDto);

    /**
     * 修改物品
     *
     * @param itemVoList 物品前端封装
     * @param editSame   是否编辑同名的所有物品，1为是，0为否
     * @return 是否成功
     */
    Boolean updateItem(List<ItemVo> itemVoList, Integer editSame);

    /**
     * 将物品加入某一类型
     *
     * @param itemIdList 物品ID列表
     * @param typeId     类型ID
     * @return 是否成功
     */
    Boolean joinItemsInType(List<Long> itemIdList, Long typeId);

    /**
     * 新增物品
     *
     * @param itemDto 物品数据封装
     * @return 新物品ID
     */
    Long createItem(ItemDto itemDto);

    /**
     * 复制物品到地区
     *
     * @param itemIdList 物品ID列表
     * @param areaId     地区ID
     * @return 物品复制到地区结果前端封装
     */
    List<Long> copyItemToArea(List<Long> itemIdList, Long areaId);

    /**
     * 删除物品
     *
     * @param itemIdList 物品ID
     * @return 是否成功
     */
    Boolean deleteItem(Long itemIdList);

    //////////////END:物品本身的接口//////////////

}
