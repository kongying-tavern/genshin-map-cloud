package site.yuanshen.genshin.core.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import site.yuanshen.common.web.response.R;
import site.yuanshen.common.web.response.RUtils;
import site.yuanshen.data.enums.HiddenFlagEnum;
import site.yuanshen.genshin.core.dao.MarkerDao;

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

    private final MarkerDao markerDao;

    @Operation(summary = "返回点位分页", description = "查询分页点位信息，返回压缩格式的byte数组")
    @GetMapping("/list_page_bin/{md5}")
    public byte[] listPageMarkerByBinary(@Parameter(hidden = true) @RequestHeader(value = "userDataLevel",required = false) String userDataLevel, @PathVariable("md5") String md5) {
        return markerDao.listPageMarkerByBinary(HiddenFlagEnum.getFlagList(userDataLevel), md5);
    }

    @Operation(summary = "返回点位分页的md5数组", description = "返回点位分页的md5数组")
    @GetMapping("/list_page_bin_md5")
    public R<List<String>> listMarkerBinaryMD5(@Parameter(hidden = true) @RequestHeader(value = "userDataLevel",required = false) String userDataLevel) {
        return RUtils.create(
                markerDao.listMarkerMD5(HiddenFlagEnum.getFlagList(userDataLevel))
        );
    }
}
