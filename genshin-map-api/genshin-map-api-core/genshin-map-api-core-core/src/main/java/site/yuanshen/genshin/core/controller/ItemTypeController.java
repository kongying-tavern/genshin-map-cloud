package site.yuanshen.genshin.core.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import site.yuanshen.common.web.response.R;
import site.yuanshen.common.web.response.RUtils;
import site.yuanshen.data.dto.ItemTypeDto;
import site.yuanshen.data.dto.helper.PageAndTypeListDto;
import site.yuanshen.data.vo.ItemTypeVo;
import site.yuanshen.data.vo.helper.PageAndTypeListVo;
import site.yuanshen.data.vo.helper.PageListVo;
import site.yuanshen.genshin.core.service.CacheService;
import site.yuanshen.genshin.core.service.ItemTypeService;

import java.util.List;

/**
 * 物品分类 Controller 层
 *
 * @author Alex Fang
 * @since 2022-10-16
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/item_type")
@Tag(name = "item_type", description = "物品分类API")
public class ItemTypeController {

    private final ItemTypeService itemTypeService;
    private final CacheService cacheService;

    //////////////START:物品类型的API//////////////

    @Operation(summary = "列出物品类型", description = "不递归遍历，只遍历子级；{self}表示查询自身还是查询子级，0为查询自身，1为查询子级")
    @PostMapping("/get/list/{self}")
    public R<PageListVo<ItemTypeVo>> listItemType(@RequestHeader(value = "isTestUser",required = false) String isTestUser,@RequestBody PageAndTypeListVo pageAndTypeListVo, @PathVariable("self") Integer self) {
        return RUtils.create(
                itemTypeService.listItemType(new PageAndTypeListDto(pageAndTypeListVo), self, StringUtils.hasLength(isTestUser))
        );
    }

    @Operation(summary = "列出所有物品类型", description = "返回所有可访问的物品类型")
    @PostMapping("/get/list_all")
    public R<List<ItemTypeVo>> listItemType(@RequestHeader(value = "isTestUser",required = false) String isTestUser) {
        return RUtils.create(
                itemTypeService.listAllItemType(StringUtils.hasLength(isTestUser))
        );
    }

    @Operation(summary = "添加物品类型", description = "成功后返回新的类型ID")
    @PutMapping("/add")
    public R<Long> addItemType(@RequestBody ItemTypeVo itemTypeVo) {
        return RUtils.create(
                itemTypeService.addItemType(new ItemTypeDto(itemTypeVo))
        );
    }

    @Operation(summary = "修改物品类型", description = "修改物品类型")
    @PostMapping("/update")
    public R<Boolean> updateItemType(@RequestBody ItemTypeVo itemTypeVo) {
        Boolean result = itemTypeService.updateItemType(new ItemTypeDto(itemTypeVo));
        cacheService.cleanItemCache();
        return RUtils.create(result);
    }

    @Operation(summary = "批量移动类型为目标类型的子类型", description = "将类型批量移动到某个类型下作为其子类型")
    @PostMapping("/move/{targetTypeId}")
    public R<Boolean> moveItemType(@RequestBody List<Long> itemTypeIdList, @PathVariable("targetTypeId") Long targetTypeId) {
        Boolean result = itemTypeService.moveItemType(itemTypeIdList, targetTypeId);
        cacheService.cleanItemCache();
        return RUtils.create(result);
    }

    @Operation(summary = "删除物品类型", description = "批量递归删除物品类型，需在前端做二次确认")
    @DeleteMapping("/delete/{itemTypeId}")
    public R<Boolean> deleteItemType(@PathVariable("itemTypeId") Long itemTypeId) {
        Boolean result = itemTypeService.deleteItemType(itemTypeId);
        cacheService.cleanItemCache();
        return RUtils.create(result);
    }

    //////////////END:物品类型的API//////////////
}