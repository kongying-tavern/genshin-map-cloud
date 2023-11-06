package site.yuanshen.genshin.core.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import site.yuanshen.common.web.response.R;
import site.yuanshen.common.web.response.RUtils;
import site.yuanshen.data.vo.MarkerLinkageSearchVo;
import site.yuanshen.data.vo.MarkerLinkageVo;
import site.yuanshen.genshin.core.service.CacheService;
import site.yuanshen.genshin.core.service.MarkerLinkageService;

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
@RequestMapping("/api/marker_linkage")
@Tag(name = "area", description = "点位关联API")
public class MarkerLinkageController {

    private final MarkerLinkageService markerLinkageService;
    private final CacheService cacheService;

    @Operation(summary = "关联点位列表", description = "关联点位列表")
    @PostMapping("/get/list")
    public R<Map<String, List<MarkerLinkageVo>>> getList(@RequestBody MarkerLinkageSearchVo markerLinkageSearchVo) {
        return RUtils.create(
                markerLinkageService.listMarkerLinkage(markerLinkageSearchVo)
        );
    }

    @Operation(summary = "关联点位", description = "关联点位数据")
    @PostMapping("/link")
    public R<String> linkMarker(@RequestBody List<MarkerLinkageVo> markerLinkageVoList) {
        String groupId = markerLinkageService.linkMarker(markerLinkageVoList);
        cacheService.cleanMarkerLinkageCache();
        return RUtils.create(groupId);
    }
}
