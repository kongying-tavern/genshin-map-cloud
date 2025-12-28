package site.yuanshen.genshin.core.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import site.yuanshen.common.core.exception.GenshinApiException;
import site.yuanshen.common.core.utils.CompressUtils;
import site.yuanshen.common.web.response.R;
import site.yuanshen.common.web.response.RUtils;
import site.yuanshen.data.enums.HiddenFlagEnum;
import site.yuanshen.data.vo.BinaryMD5Vo;
import site.yuanshen.genshin.core.dao.MarkerDao;
import site.yuanshen.genshin.core.service.MarkerDocService;

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
@RequestMapping("/api/marker_doc")
@Tag(name = "marker_doc", description = "点位档案API")
public class MarkerDocController {

    private final MarkerDao markerDao;
    private final MarkerDocService markerDocService;

    @Operation(summary = "返回点位分页", description = "查询分页点位信息，返回压缩格式的byte数组")
    @GetMapping("/list_page_bin/{md5}")
    public byte[] listPageMarkerByBinary(@Parameter(hidden = true) @RequestHeader(value = "userDataLevel", required = false) String userDataLevel, @PathVariable("md5") String md5) {
        return markerDao.getMarkerBinary(HiddenFlagEnum.getFlagListByMask(userDataLevel), md5);
    }

    @Operation(summary = "返回点位分页的md5数组", description = "返回点位分页的md5数组")
    @GetMapping("/list_page_bin_md5")
    public R<List<BinaryMD5Vo>> listMarkerBinaryMD5(@Parameter(hidden = true) @RequestHeader(value = "userDataLevel", required = false) String userDataLevel) {
        return RUtils.create(
            markerDao.listMarkerBinaryMD5(HiddenFlagEnum.getFlagListByMask(userDataLevel))
        );
    }

    @Operation(summary = "返回点位差异比对快照", description = "点位差异比对二进制快照，快照为protobuf数据")
    @GetMapping("/list_diff_snapshot")
    public byte[] listMarkerDiffSnapshotByBinary(@Parameter(hidden = true) @RequestHeader(value = "userDataLevel", required = false) String userDataLevel) {
        return markerDocService.getMarkerDiffSnapshot(HiddenFlagEnum.getFlagListByMask(userDataLevel));
    }

    @Operation(summary = "返回所有点位", description = "点位列表二进制，二进制位protobuf数据+压缩")
    @GetMapping("/list_markers")
    public byte[] listMarkersByBinary(@Parameter(hidden = true) @RequestHeader(value = "userDataLevel", required = false) String userDataLevel) {
        try {
            return CompressUtils.compress(
                markerDocService.getMarkerList(HiddenFlagEnum.getFlagListByMask(userDataLevel))
            );
        } catch (IOException e) {
            throw new GenshinApiException("生成数据错误");
        }
    }
}
