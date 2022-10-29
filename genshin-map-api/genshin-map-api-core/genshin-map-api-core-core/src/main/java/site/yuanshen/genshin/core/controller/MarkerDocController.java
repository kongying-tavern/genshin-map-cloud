package site.yuanshen.genshin.core.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import site.yuanshen.common.web.response.R;
import site.yuanshen.common.web.response.RUtils;
import site.yuanshen.genshin.core.dao.MarkerDao;

import java.io.IOException;
import java.util.List;

/**
 * 点位档案 Controller 层
 *
 * @author Alex Fang
 * @since 2022-10-16
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/marker_doc")
@Tag(name = "marker_doc", description = "点位档案API")
public class MarkerDocController {

    private final MarkerDao markerDao;

    @Operation(summary = "通过bz2返回点位分页", description = "查询分页点位信息，返回bz2压缩格式的byte数组")
    @GetMapping("/get/list_page_bz2/{index}")
    public byte[] listPageMarkerBy7zip(@RequestHeader(value = "isTestUser", required = false) String isTestUser,
                                      @PathVariable("index") Integer index) throws IOException {
        return markerDao.listPageMarkerByBz2(StringUtils.hasLength(isTestUser),index);
    }

    @Operation(summary = "返回点位分页bz2的md5数组", description = "返回点位分页bz2的md5数组")
    @GetMapping("/get/list_page_bz2_md5")
    public R<List<String>> listMarkerBz2MD5(@RequestHeader(value = "isTestUser", required = false) String isTestUser) {
        return RUtils.create(
                markerDao.listMarkerBz2MD5(StringUtils.hasLength(isTestUser))
        );
    }
}
