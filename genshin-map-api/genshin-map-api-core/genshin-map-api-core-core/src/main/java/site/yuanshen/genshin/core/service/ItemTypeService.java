package site.yuanshen.genshin.core.service;

import site.yuanshen.data.dto.ItemTypeDto;
import site.yuanshen.data.dto.helper.PageAndTypeSearchDto;
import site.yuanshen.data.vo.ItemTypeVo;
import site.yuanshen.data.vo.helper.PageListVo;

import java.util.List;

/**
 * 物品类型服务接口
 *
 * @author Alex Fang
 */
public interface ItemTypeService {

    //////////////START:物品类型的接口//////////////

    /**
     * 列出物品类型
     *
     * @param searchDto 带分类的分页查询数据封装
     * @param self      查询自身还是查询子级，0为查询自身，1为查询子级
     * @param hiddenFlagList hidden_flag范围
     * @return 物品类型的前端封装的分页封装
     */
    PageListVo<ItemTypeVo> listItemType(PageAndTypeSearchDto searchDto, Integer self, List<Integer> hiddenFlagList);

    /**
     * 列出所有物品类型
     *
     * @param hiddenFlagList hidden_flag范围
     * @return 物品类型的前端封装的列表
     */
    List<ItemTypeVo> listAllItemType(List<Integer> hiddenFlagList);

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
     * @param itemTypeId 类型ID列表
     * @return 是否成功
     */
    Boolean deleteItemType(Long itemTypeId);

    //////////////END:物品类型的接口//////////////

}
