package site.yuanshen.genshin.core.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import site.yuanshen.common.web.response.R;
import site.yuanshen.common.web.response.RUtils;
import site.yuanshen.genshin.core.dao.ItemDao;

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

    @Operation(summary = "通过bz2返回所有物品信息", description = "查询所有物品信息，返回bz2压缩格式的byte数组")
    @GetMapping("/all_bz2")
    public byte[] listAllItemBz2() {
        return itemDao.listAllItemBz2();
    }

    @Operation(summary = "返回所有物品信息bz2的md5", description = "返回所有物品信息bz2的md5")
    @GetMapping("/all_bz2_md5")
    public R<String> listAllItemBz2Md5() {
        return RUtils.create(itemDao.listAllItemBz2Md5());
    }
}
