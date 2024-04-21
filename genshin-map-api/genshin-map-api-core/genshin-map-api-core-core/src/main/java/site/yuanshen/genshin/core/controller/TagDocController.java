package site.yuanshen.genshin.core.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import site.yuanshen.common.web.response.R;
import site.yuanshen.common.web.response.RUtils;
import site.yuanshen.genshin.core.dao.IconTagDao;

/**
 * 图标标签档案 Controller 层
 *
 * @author Alex Fang
 * @since 2022-10-15
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/tag_doc")
@Tag(name = "tag_doc", description = "图标标签档案API")
public class TagDocController {

    private final IconTagDao iconTagDao;

    @Operation(summary = "获取所有标签信息的压缩", description = "查询所有标签信息，返回压缩格式的byte数组")
    @GetMapping("/all_bin")
    public byte[] listAllTagBinary() {
        return iconTagDao.listAllTagBinary();
    }

    @Operation(summary = "返回所有标签信息的md5", description = "返回所有标签信息的md5")
    @GetMapping("/all_bin_md5")
    public R<String> listAllTagBinaryMd5() {
        return RUtils.create(
                iconTagDao.listAllTagBinaryMd5()
        );
    }
}
