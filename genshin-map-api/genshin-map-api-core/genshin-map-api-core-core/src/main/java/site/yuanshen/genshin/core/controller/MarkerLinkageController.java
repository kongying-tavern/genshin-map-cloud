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
import site.yuanshen.data.vo.MarkerLinkageVo;
import site.yuanshen.genshin.core.service.MarkerLinkService;

import java.util.List;

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

    private final MarkerLinkService markerLinkService;

    @Operation(summary = "关联点位", description = "关联点位数据")
    @PostMapping("/link")
    public R<String> linkMarker(@RequestBody List<MarkerLinkageVo> markerLinkageVoList) {
        return RUtils.create(
                markerLinkService.linkMarker(markerLinkageVoList)
        );
    }
}
