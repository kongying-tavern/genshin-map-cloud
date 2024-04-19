package site.yuanshen.genshin.core.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import site.yuanshen.common.web.response.R;
import site.yuanshen.common.web.response.RUtils;
import site.yuanshen.genshin.core.dao.MarkerLinkageDao;
import site.yuanshen.genshin.core.service.MarkerLinkageDocService;

/**
 * 点位关联 Controller 层
 *
 * @author Alex Fang
 * @since 2023-11-12
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/marker_link_doc")
@Tag(name = "marker_link_doc", description = "点位关联档案API")
public class MarkerLinkageDocController {
    private final MarkerLinkageDao markerLinkageDao;
    private final MarkerLinkageDocService markerLinkageDocService;

    @Operation(summary = "返回所有点位关联列表", description = "查询所有点位关联列表，返回压缩格式的byte数组")
    @GetMapping("/all_list_bin")
    public byte[] listAllMarkerLinkageBinary() {
        return markerLinkageDao.listAllMarkerLinkageBinary();
    }

    @Operation(summary = "返回所有点位关联列表的md5", description = "返回所有点位关联列表的md5")
    @GetMapping("/all_list_bin_md5")
    public R<String> listAllMarkerLinkageBinaryMD5() {
        return RUtils.create(markerLinkageDocService.listMarkerLinkageBinaryMD5());
    }

    @Operation(summary = "返回所有点位关联有向图数据", description = "查询所有点位关联有向图数据，返回压缩格式的byte数组")
    @GetMapping("/all_graph_bin")
    public byte[] graphAllMarkerLinkageBinary() {
        return markerLinkageDao.graphAllMarkerLinkageBinary();
    }

    @Operation(summary = "返回所有点位关联有向图数据的md5", description = "返回所有点位关联有向图数据的md5")
    @GetMapping("/all_graph_bin_md5")
    public R<String> graphAllMarkerLinkageBinaryMD5() {
        return RUtils.create(markerLinkageDocService.graphMarkerLinkageBinaryMD5());
    }
}
