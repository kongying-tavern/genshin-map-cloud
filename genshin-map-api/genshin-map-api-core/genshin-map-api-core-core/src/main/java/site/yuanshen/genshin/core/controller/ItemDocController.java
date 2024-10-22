package site.yuanshen.genshin.core.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import site.yuanshen.common.web.response.R;
import site.yuanshen.common.web.response.RUtils;
import site.yuanshen.data.enums.HiddenFlagEnum;
import site.yuanshen.genshin.core.dao.ItemDao;

import java.util.List;

/**
 * 物品档案 Controller 层
 *
 * @author Alex Fang
 * @since 2022-10-16
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/item_doc")
@Tag(name = "item_doc", description = "物品档案API")
public class ItemDocController {

    private final ItemDao itemDao;

    @Operation(summary = "返回物品分页", description = "查询分页物品信息，返回压缩格式的byte数组")
    @GetMapping("/list_page_bin/{md5}")
    public byte[] listPageItemByBinary(@Parameter(hidden = true) @RequestHeader(value = "userDataLevel",required = false) String userDataLevel, @PathVariable("md5") String md5) {
        return itemDao.getItemBinary(HiddenFlagEnum.getFlagListByMask(userDataLevel), md5);
    }

    @Operation(summary = "返回物品分页的md5数组", description = "返回物品分页的md5数组")
    @GetMapping("/list_page_bin_md5")
    public R<List<String>> listItemBinaryMD5(@Parameter(hidden = true) @RequestHeader(value = "userDataLevel",required = false) String userDataLevel) {
        return RUtils.create(
            itemDao.listItemBinaryMD5(HiddenFlagEnum.getFlagListByMask(userDataLevel))
        );
    }
}
