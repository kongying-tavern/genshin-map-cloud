package site.yuanshen.genshin.core.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import site.yuanshen.common.web.response.R;
import site.yuanshen.common.web.response.RUtils;
import site.yuanshen.common.web.response.WUtils;
import site.yuanshen.data.dto.MarkerDto;
import site.yuanshen.data.dto.helper.PageSearchDto;
import site.yuanshen.data.enums.HiddenFlagEnum;
import site.yuanshen.data.vo.MarkerSearchVo;
import site.yuanshen.data.vo.MarkerVo;
import site.yuanshen.data.vo.adapter.marker.tweak.TweakVo;
import site.yuanshen.data.vo.helper.PageListVo;
import site.yuanshen.data.vo.helper.PageSearchVo;
import site.yuanshen.genshin.core.service.CacheService;
import site.yuanshen.genshin.core.service.MarkerService;
import site.yuanshen.genshin.core.service.UserAppenderService;
import site.yuanshen.genshin.core.websocket.WebSocketEntrypoint;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 点位 Controller 层
 *
 * @author Moment
 * @since 2022-06-11
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/marker")
@Tag(name = "marker", description = "点位API")
public class MarkerController {

    private final MarkerService markerService;
    private final CacheService cacheService;
    private final WebSocketEntrypoint webSocket;

    //////////////START:点位自身的API//////////////

    @Operation(summary = "根据各种条件筛选查询点位ID",
            description = "支持根据末端地区、末端类型、物品来进行查询，三种查询不能同时生效，同时存在时报错，同时支持测试点位获取")
    @PostMapping("/get/id")
    public R<List<Long>> searchMarkerId(@Parameter(hidden = true) @RequestHeader(value = "userDataLevel", required = false) String userDataLevel, @RequestBody MarkerSearchVo markerSearchVo) {
        return RUtils.create(
                markerService.searchMarkerId(markerSearchVo, HiddenFlagEnum.getFlagListByMask(userDataLevel))
        );
    }

    @Operation(summary = "根据各种条件筛选查询点位信息",
            description = "支持根据末端地区、末端类型、物品来进行查询，三种查询不能同时生效，同时存在时报错，同时支持测试点位获取")
    @PostMapping("/get/list_byinfo")
    public R<List<MarkerVo>> searchMarker(@Parameter(hidden = true) @RequestHeader(value = "userDataLevel", required = false) String userDataLevel, @RequestBody MarkerSearchVo markerSearchVo) {
        R<List<MarkerVo>> result = RUtils.create(
                markerService.searchMarker(markerSearchVo, HiddenFlagEnum.getFlagListByMask(userDataLevel))
        );
        UserAppenderService.appendUser(result, result.getData(), true, MarkerVo::getCreatorId);
        UserAppenderService.appendUser(result, result.getData(), true, MarkerVo::getUpdaterId);
        return result;
    }

    @Operation(summary = "通过ID列表查询点位信息", description = "通过ID列表来进行查询点位信息")
    @PostMapping("/get/list_byid")
    public R<List<MarkerVo>> listMarkerById(@Parameter(hidden = true) @RequestHeader(value = "userDataLevel", required = false) String userDataLevel, @RequestBody List<Long> markerIdList) {
        R<List<MarkerVo>> result = RUtils.create(
                markerService.listMarkerById(markerIdList, HiddenFlagEnum.getFlagListByMask(userDataLevel))
        );
        UserAppenderService.appendUser(result, result.getData(), true, MarkerVo::getCreatorId);
        UserAppenderService.appendUser(result, result.getData(), true, MarkerVo::getUpdaterId);
        return result;
    }

    @Operation(summary = "分页查询所有点位信息", description = "分页查询所有点位信息")
    @PostMapping("/get/page")
    public R<PageListVo<MarkerVo>> listMarkerPage(@Parameter(hidden = true) @RequestHeader(value = "userDataLevel", required = false) String userDataLevel, @RequestBody PageSearchVo pageSearchVo) {
        R<PageListVo<MarkerVo>> result = RUtils.create(
                markerService.listMarkerPage(new PageSearchDto(pageSearchVo), HiddenFlagEnum.getFlagListByMask(userDataLevel))
        );
        UserAppenderService.appendUser(result, result.getData().getRecord(), true, MarkerVo::getCreatorId);
        UserAppenderService.appendUser(result, result.getData().getRecord(), true, MarkerVo::getUpdaterId);
        return result;
    }

    @Operation(summary = "新增点位（不包括额外字段）", description = "新增完成后返回点位ID")
    @PutMapping("/single")
    public R<Long> createMarker(@RequestBody MarkerVo markerVo) {
        Long newId = markerService.createMarker(new MarkerDto(markerVo));
        cacheService.cleanItemCache();
        cacheService.cleanMarkerCache();
        // For new marker, no need to clean marker linkage related path cache
        // since new marker will not be linked in path list.
        webSocket.broadcast(WUtils.create("MarkerAdded", newId));
        return RUtils.create(newId);
    }

    @Operation(summary = "修改点位（不包括额外字段）", description = "根据点位ID修改点位")
    @PostMapping("/single")
    public R<Boolean> updateMarker(@RequestBody MarkerVo markerVo) {
        Boolean result = markerService.updateMarker(new MarkerDto(markerVo));
        cacheService.cleanItemCache();
        cacheService.cleanMarkerCache();
        cacheService.cleanMarkerLinkageCache();
        webSocket.broadcast(WUtils.create("MarkerUpdated", markerVo.getId()));
        return RUtils.create(result);
    }

    @Operation(summary = "删除点位", description = "根据点位ID列表批量删除点位")
    @DeleteMapping("/{markerId}")
    public R<Boolean> deleteMarker(@PathVariable("markerId") Long markerId) {
        Boolean result = markerService.deleteMarker(markerId);
        cacheService.cleanItemCache();
        cacheService.cleanMarkerCache();
        cacheService.cleanMarkerLinkageCache();
        webSocket.broadcast(WUtils.create("MarkerDeleted", markerId));
        return RUtils.create(result);
    }


    //////////////END:点位自身的API//////////////

    //////////////START:点位调整的API//////////////
    @Operation(summary = "调整点位", description = "对点位数据进行微调")
    @PostMapping("/tweak")
    public R<List<MarkerVo>> tweakMarkers(@RequestBody TweakVo tweakVo) {
        List<MarkerVo> result = markerService.tweakMarkers(tweakVo);
        cacheService.cleanItemCache();
        cacheService.cleanMarkerCache();
        cacheService.cleanMarkerLinkageCache();
        webSocket.broadcast(WUtils.create("MarkerTweaked", result.parallelStream().map(MarkerVo::getId).collect(Collectors.toList())));
        return RUtils.create(result);
    }
    //////////////END:点位调整的API//////////////

}
