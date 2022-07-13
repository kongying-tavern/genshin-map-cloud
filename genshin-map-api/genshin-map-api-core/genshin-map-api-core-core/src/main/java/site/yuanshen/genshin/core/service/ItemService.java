package site.yuanshen.genshin.core.service;

import site.yuanshen.data.dto.ItemDto;
import site.yuanshen.data.dto.ItemSearchDto;
import site.yuanshen.data.dto.ItemTypeDto;
import site.yuanshen.data.dto.helper.PageAndTypeListDto;
import site.yuanshen.data.dto.helper.PageSearchDto;
import site.yuanshen.data.vo.ItemTypeVo;
import site.yuanshen.data.vo.ItemVo;
import site.yuanshen.data.vo.helper.PageListVo;

import java.util.List;

/**
 * 物品服务接口
 *
 * @author Moment
 */
public interface ItemService {

    //////////////START:物品类型的接口//////////////

    /**
     * 列出物品类型
     *
     * @param searchDto 带分类的分页查询数据封装
     * @param self      查询自身还是查询子级，0为查询自身，1为查询子级
     * @return 物品类型的前端封装的分页封装
     */
    PageListVo<ItemTypeVo> listItemType(PageAndTypeListDto searchDto, Integer self);

    /**
     * 添加物品类型
     *
     * @param itemTypeDto 物品类型数据封装
     * @return 新物品类型ID
     */
    Long addItemType(ItemTypeDto itemTypeDto);

    /**
     * 修改物品类型
     *
     * @param itemTypeDto 物品类型数据封装
     * @return 是否成功
     */
    Boolean updateItemType(ItemTypeDto itemTypeDto);

    /**
     * 批量移动类型为目标类型的子类型
     *
     * @param itemTypeIdList 类型ID列表
     * @param targetTypeId   目标类型ID
     * @return 是否成功
     */
    Boolean moveItemType(List<Long> itemTypeIdList, Long targetTypeId);

    /**
     * 批量递归删除物品类型
     *
     * @param itemTypeIdList 类型ID列表
     * @return 是否成功
     */
    Boolean deleteItemType(List<Long> itemTypeIdList);

    //////////////END:物品类型的接口//////////////

    //////////////START:物品本身的接口//////////////

    /**
     * 根据物品ID查询物品
     *
     * @param itemIdList 物品ID列表
     * @return 物品数据封装列表
     */
    List<ItemDto> listItemById(List<Long> itemIdList);

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
     * @param itemIdList 物品ID列表
     * @return 是否成功
     */
    Boolean deleteItem(List<Long> itemIdList);

    //////////////END:物品本身的接口//////////////

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
     * @param itemIdList 物品ID列表
     * @return 是否成功
     */
    Boolean deleteCommonItem(List<Long> itemIdList);

    //////////////END:地区公用物品的接口//////////////

}
