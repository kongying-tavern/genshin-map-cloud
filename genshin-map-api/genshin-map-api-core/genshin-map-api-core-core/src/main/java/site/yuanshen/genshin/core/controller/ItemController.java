package site.yuanshen.genshin.core.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import site.yuanshen.common.web.response.R;
import site.yuanshen.common.web.response.RUtils;
import site.yuanshen.data.dto.ItemDto;
import site.yuanshen.data.dto.ItemSearchDto;
import site.yuanshen.data.dto.ItemTypeDto;
import site.yuanshen.data.dto.helper.PageAndTypeListDto;
import site.yuanshen.data.dto.helper.PageSearchDto;
import site.yuanshen.data.vo.ItemSearchVo;
import site.yuanshen.data.vo.ItemTypeVo;
import site.yuanshen.data.vo.ItemVo;
import site.yuanshen.data.vo.helper.PageAndTypeListVo;
import site.yuanshen.data.vo.helper.PageListVo;
import site.yuanshen.data.vo.helper.PageSearchVo;
import site.yuanshen.genshin.core.dao.ItemDao;
import site.yuanshen.genshin.core.service.CacheService;
import site.yuanshen.genshin.core.service.ItemService;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 物品 Controller 层
 *
 * @author Moment
 * @since 2022-06-15
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/item")
@Tag(name = "item", description = "物品API")
public class ItemController {

    private final ItemService itemService;
    private final ItemDao itemDao;
    private final CacheService cacheService;

    //////////////START:物品类型的API//////////////

    @Operation(summary = "列出物品类型", description = "不递归遍历，只遍历子级；{self}表示查询自身还是查询子级，0为查询自身，1为查询子级")
    @PostMapping("/get/type/{self}")
    public R<PageListVo<ItemTypeVo>> listItemType(@RequestHeader(value = "isTestUser",required = false) String isTestUser,@RequestBody PageAndTypeListVo pageAndTypeListVo, @PathVariable("self") Integer self) {
        return RUtils.create(
                itemService.listItemType(new PageAndTypeListDto(pageAndTypeListVo), self, StringUtils.hasLength(isTestUser))
        );
    }

    @Operation(summary = "添加物品类型", description = "成功后返回新的类型ID")
    @PutMapping("/type")
    @Transactional
    public R<Long> addItemType(@RequestBody ItemTypeVo itemTypeVo) {
        return RUtils.create(
                itemService.addItemType(new ItemTypeDto(itemTypeVo))
        );
    }

    @Operation(summary = "修改物品类型", description = "修改物品类型")
    @PostMapping("/type")
    @Transactional
    public R<Boolean> updateItemType(@RequestBody ItemTypeVo itemTypeVo) {
        Boolean result = itemService.updateItemType(new ItemTypeDto(itemTypeVo));
        cacheService.cleanItemCache();
        return RUtils.create(result);
    }

    @Operation(summary = "批量移动类型为目标类型的子类型", description = "将类型批量移动到某个类型下作为其子类型")
    @PostMapping("/type/move/{targetTypeId}")
    @Transactional
    public R<Boolean> moveItemType(@RequestBody List<Long> itemTypeIdList, @PathVariable("targetTypeId") Long targetTypeId) {
        Boolean result = itemService.moveItemType(itemTypeIdList, targetTypeId);
        cacheService.cleanItemCache();
        return RUtils.create(result);
    }

    @Operation(summary = "删除物品类型", description = "批量递归删除物品类型，需在前端做二次确认")
    @DeleteMapping("/type/{itemTypeId}")
    @Transactional
    public R<Boolean> deleteItemType(@PathVariable("itemTypeId") Long itemTypeId) {
        Boolean result = itemService.deleteItemType(itemTypeId);
        cacheService.cleanItemCache();
        return RUtils.create(result);
    }

    //////////////END:物品类型的API//////////////

    //////////////START:物品本身的API//////////////

    @Operation(summary = "通过bz2返回所有物品信息", description = "查询所有物品信息，返回bz2压缩格式的byte数组")
    @GetMapping("/get/all_bz2")
    public byte[] listAllItemBz2() {
        return itemDao.listAllItemBz2();
    }

    @Operation(summary = "返回所有物品信息bz2的md5", description = "返回所有物品信息bz2的md5")
    @GetMapping("/get/all_bz2_md5")
    public R<String> listAllItemBz2Md5() {
        return RUtils.create(itemDao.listAllItemBz2Md5());
    }

    @Operation(summary = "根据物品ID查询物品", description = "输入ID列表查询，单个查询也用此API")
    @PostMapping("/get/list_byid")
    public R<List<ItemVo>> listItemById(@RequestHeader(value = "isTestUser",required = false) String isTestUser,@RequestBody List<Long> itemIdList) {
        return RUtils.create(
                itemService.listItemById(itemIdList,StringUtils.hasLength(isTestUser))
                        .stream().map(ItemDto::getVo).collect(Collectors.toList())
        );
    }

    @Operation(summary = "根据筛选条件列出物品信息", description = "传入的物品类型ID和地区ID列表，必须为末端的类型或地区")
    @PostMapping("/get/list")
    public R<PageListVo<ItemVo>> listItemIdByType(@RequestHeader(value = "isTestUser",required = false) String isTestUser,@RequestBody ItemSearchVo itemSearchVo) {
        return RUtils.create(
                itemService.listItem(new ItemSearchDto(itemSearchVo).setIsTestUser(StringUtils.hasLength(isTestUser)))
        );
    }

    @Operation(summary = "修改物品", description = "提供修改同名物品功能，默认关闭")
    @PostMapping("/update/{editSame}")
    @Transactional
    public R<Boolean> updateItem(@RequestBody List<ItemVo> itemVoList, @PathVariable("editSame") Integer editSame) {
        Boolean result = itemService.updateItem(itemVoList, editSame);
        cacheService.cleanItemCache();
        return RUtils.create(result);
    }

    @Operation(summary = "将物品加入某一类型", description = "根据物品ID列表批量加入，在加入多类型时需要注意类型的地区需一致，不一致会直接报错")
    @PostMapping("/join/{typeId}")
    @Transactional
    public R<Boolean> joinItemsInType(@RequestBody List<Long> itemIdList, @PathVariable("typeId") Long typeId) {
        Boolean result = itemService.joinItemsInType(itemIdList, typeId);
        cacheService.cleanItemCache();
        return RUtils.create(result);
    }

    @Operation(summary = "新增物品", description = "新建成功后会返回新物品ID")
    @PutMapping("")
    @Transactional
    public R<Long> createItem(@RequestBody ItemVo itemVo) {
        Long newId = itemService.createItem(new ItemDto(itemVo));
        cacheService.cleanItemCache();
        return RUtils.create(newId);
    }

    @Operation(summary = "复制物品到地区", description = "此操作估计会占用较长时间，根据物品ID列表复制物品到新地区，此操作会递归复制类型及父级类型。会返回新的物品列表与新的类型列表，用于反映新的ID")
    @PutMapping("/copy/{areaId}")
    @Transactional
    public R<List<Long>> copyItemToArea(@RequestBody List<Long> itemIdList, @PathVariable("areaId") Long areaId) {
        List<Long> idList = itemService.copyItemToArea(itemIdList, areaId);
        cacheService.cleanItemCache();
        return RUtils.create(idList);
    }

    @Operation(summary = "删除物品", description = "根据物品ID列表批量删除物品")
    @DeleteMapping("/{itemId}")
    @Transactional
    public R<Boolean> deleteItem(@PathVariable("itemId")Long itemId) {
        Boolean result = itemService.deleteItem(itemId);
        if (result) {
            cacheService.cleanItemCache();
            cacheService.cleanMarkerCache();
        }
        return RUtils.create(result);
    }

    //////////////END:物品本身的API//////////////

    //////////////START:地区公用物品的API//////////////

    @Operation(summary = "列出地区公用物品", description = "列出地区公用物品")
    @PostMapping("/get/common")
    public R<PageListVo<ItemVo>> listCommonItem(@RequestBody PageSearchVo pageSearchVo) {
        return RUtils.create(
                itemService.listCommonItem(new PageSearchDto(pageSearchVo))
        );
    }

    @Operation(summary = "新增地区公用物品", description = "通过ID列表批量添加地区公用物品")
    @PutMapping("/common")
    @Transactional
    public R<Boolean> addCommonItem(@RequestBody List<Long> itemIdList) {
        return RUtils.create(
                itemService.addCommonItem(itemIdList)
        );
    }

    @Operation(summary = "删除地区公用物品", description = "通过ID列表批量删除地区公用物品")
    @DeleteMapping("/common")
    @Transactional
    public R<Boolean> deleteCommonItem(@PathVariable("itemId")Long itemId) {
        return RUtils.create(
                itemService.deleteCommonItem(itemId)
        );
    }

    //////////////END:地区公用物品的API//////////////
}
