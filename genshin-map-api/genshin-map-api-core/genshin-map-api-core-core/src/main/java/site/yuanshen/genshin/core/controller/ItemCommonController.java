package site.yuanshen.genshin.core.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import site.yuanshen.common.web.response.R;
import site.yuanshen.common.web.response.RUtils;
import site.yuanshen.data.dto.helper.PageSearchDto;
import site.yuanshen.data.vo.ItemAreaPublicVo;
import site.yuanshen.data.vo.helper.PageListVo;
import site.yuanshen.data.vo.helper.PageSearchVo;
import site.yuanshen.genshin.core.service.ItemCommonService;

import java.util.List;

/**
 * 公用物品 Controller 层
 *
 * @author Alex
 * @since 2022-10-16
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/item_common")
@Tag(name = "item_common", description = "公用物品API")
public class ItemCommonController {

    private final ItemCommonService itemCommonService;

    //////////////START:地区公用物品的API//////////////

    @Operation(summary = "列出地区公用物品", description = "列出地区公用物品")
    @PostMapping("/get/list")
    public R<PageListVo<ItemAreaPublicVo>> listCommonItem(@RequestBody PageSearchVo pageSearchVo) {
        return RUtils.create(
                itemCommonService.listCommonItem(new PageSearchDto(pageSearchVo))
        );
    }

    @Operation(summary = "新增地区公用物品", description = "通过ID列表批量添加地区公用物品")
    @PutMapping("/add")
    public R<Boolean> addCommonItem(@RequestBody List<Long> itemIdList) {
        return RUtils.create(
                itemCommonService.addCommonItem(itemIdList)
        );
    }

    @Operation(summary = "删除地区公用物品", description = "通过ID列表批量删除地区公用物品")
    @DeleteMapping("/delete/{itemId}")
    public R<Boolean> deleteCommonItem(@PathVariable("itemId")Long itemId) {
        return RUtils.create(
                itemCommonService.deleteCommonItem(itemId)
        );
    }

    //////////////END:地区公用物品的API//////////////
}
