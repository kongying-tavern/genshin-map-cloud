package site.yuanshen.genshin.core.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import site.yuanshen.common.web.response.R;
import site.yuanshen.common.web.response.RUtils;
import site.yuanshen.data.vo.BinaryMD5Vo;
import site.yuanshen.genshin.core.dao.IconDao;

/**
 * 图标档案 Controller 层
 *
 * @author Alex Fang
 * @since 2022-10-15
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/icon_doc")
@Tag(name = "icon_doc", description = "图标档案API")
public class IconDocController {

    private final IconDao iconDao;

    @Operation(summary = "获取所有图标信息的压缩", description = "查询所有图标信息，返回压缩格式的byte数组")
    @GetMapping("/all_bin")
    public byte[] listAllIconBinary() {
        return iconDao.listAllIconBinary();
    }

    @Operation(summary = "返回所有图标信息的md5", description = "返回所有图标信息的md5")
    @GetMapping("/all_bin_md5")
    public R<BinaryMD5Vo> listAllIconBinaryMd5() {
        return RUtils.create(
            iconDao.listAllIconBinaryMd5()
        );
    }
}
