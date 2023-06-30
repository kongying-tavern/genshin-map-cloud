package site.yuanshen.genshin.core.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import site.yuanshen.common.web.response.R;
import site.yuanshen.common.web.response.RUtils;
import site.yuanshen.genshin.core.dao.MarkerDao;
import site.yuanshen.genshin.core.service.MarkerDocService;

import java.util.List;

/**
 * 点位档案 Controller 层
 *
 * @author Alex Fang
 * @since 2022-10-16
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/marker_doc")
@Tag(name = "marker_doc", description = "点位档案API")
public class MarkerDocController {

    private final MarkerDocService markerDocService;
    private final MarkerDao markerDao;

    @Operation(summary = "通过bz2返回点位分页", description = "查询分页点位信息，返回bz2压缩格式的byte数组")
    @GetMapping("/list_page_bz2/{index}")
    public byte[] listPageMarkerBy7zip(@PathVariable("index") Integer index) {
        return markerDao.listPageMarkerByBz2(index);
    }

    @Operation(summary = "返回点位分页bz2的md5数组", description = "返回点位分页bz2的md5数组")
    @GetMapping("/list_page_bz2_md5")
    public R<List<String>> listMarkerBz2MD5() {
        return RUtils.create(
                markerDocService.listMarkerBz2MD5()
        );
    }
}
