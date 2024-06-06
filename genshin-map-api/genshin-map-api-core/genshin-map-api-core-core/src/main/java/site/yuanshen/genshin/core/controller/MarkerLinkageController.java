package site.yuanshen.genshin.core.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import site.yuanshen.common.web.response.R;
import site.yuanshen.common.web.response.RUtils;
import site.yuanshen.common.web.response.WUtils;
import site.yuanshen.data.vo.MarkerLinkageSearchVo;
import site.yuanshen.data.vo.MarkerLinkageVo;
import site.yuanshen.data.vo.adapter.marker.linkage.LinkChangeVo;
import site.yuanshen.data.vo.adapter.marker.linkage.graph.GraphVo;
import site.yuanshen.genshin.core.service.CacheService;
import site.yuanshen.genshin.core.service.MarkerLinkageService;
import site.yuanshen.genshin.core.websocket.WebSocketEntrypoint;

import java.util.List;
import java.util.Map;

/**
 * 点位关联 Controller 层
 *
 * @author Alex Fang
 * @since 2023-10-21
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/marker_link")
@Tag(name = "marker_link", description = "点位关联API")
public class MarkerLinkageController {

    private final MarkerLinkageService markerLinkageService;
    private final CacheService cacheService;
    private final WebSocketEntrypoint webSocket;

    @Operation(summary = "关联点位列表", description = "关联点位列表")
    @PostMapping("/get/list")
    public R<Map<String, List<MarkerLinkageVo>>> getMarkerLinkageList(@RequestBody MarkerLinkageSearchVo markerLinkageSearchVo) {
        return RUtils.create(
                markerLinkageService.listMarkerLinkage(markerLinkageSearchVo)
        );
    }

    @Operation(summary = "关联点位图数据", description = "关联点位图数据")
    @PostMapping("/get/graph")
    public R<Map<String, GraphVo>> getMarkerLinkageGraph(@RequestBody MarkerLinkageSearchVo markerLinkageSearchVo) {
        return RUtils.create(
                markerLinkageService.graphMarkerLinkage(markerLinkageSearchVo)
        );
    }

    @Operation(summary = "关联点位", description = "关联点位数据")
    @PostMapping("/link")
    public R<String> linkMarker(@RequestBody List<MarkerLinkageVo> markerLinkageVoList) {
        LinkChangeVo linkChangeVo = new LinkChangeVo();
        String groupId = markerLinkageService.linkMarker(markerLinkageVoList, linkChangeVo);
        cacheService.cleanMarkerCache();
        cacheService.cleanMarkerLinkageCache();
        webSocket.broadcast(WUtils.create("MarkerLinked", linkChangeVo));
        return RUtils.create(groupId);
    }
}
