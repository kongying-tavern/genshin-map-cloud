package site.yuanshen.genshin.core.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import site.yuanshen.common.web.response.R;
import site.yuanshen.common.web.response.RUtils;
import site.yuanshen.data.dto.MarkerDto;
import site.yuanshen.data.dto.MarkerExtraDto;
import site.yuanshen.data.dto.MarkerSingleDto;
import site.yuanshen.data.dto.helper.PageSearchDto;
import site.yuanshen.data.vo.MarkerExtraVo;
import site.yuanshen.data.vo.MarkerSearchVo;
import site.yuanshen.data.vo.MarkerSingleVo;
import site.yuanshen.data.vo.MarkerVo;
import site.yuanshen.data.vo.helper.PageListVo;
import site.yuanshen.data.vo.helper.PageSearchVo;
import site.yuanshen.genshin.core.service.CacheService;
import site.yuanshen.genshin.core.service.MarkerService;

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
@RequestMapping("/marker")
@Tag(name = "marker", description = "点位API")
public class MarkerController {

    private final MarkerService markerService;
    private final CacheService cacheService;

    //////////////START:点位自身的API//////////////

    @Operation(summary = "根据各种条件筛选查询点位ID",
            description = "支持根据末端地区、末端类型、物品来进行查询，三种查询不能同时生效，同时存在时报错，同时支持测试点位获取")
    @PostMapping("/get/id")
    public R<List<Long>> searchMarkerId(@RequestHeader(value = "isTestUser", required = false) String isTestUser, @RequestBody MarkerSearchVo markerSearchVo) {
        markerSearchVo.setIsTestUser(StringUtils.hasLength(isTestUser));
        return RUtils.create(
                markerService.searchMarkerId(markerSearchVo)
        );
    }

    @Operation(summary = "根据各种条件筛选查询点位信息",
            description = "支持根据末端地区、末端类型、物品来进行查询，三种查询不能同时生效，同时存在时报错，同时支持测试点位获取")
    @PostMapping("/get/list_byinfo")
    public R<List<MarkerVo>> searchMarker(@RequestHeader(value = "isTestUser", required = false) String isTestUser, @RequestBody MarkerSearchVo markerSearchVo) {
        markerSearchVo.setIsTestUser(StringUtils.hasLength(isTestUser));
        return RUtils.create(
                markerService.searchMarker(markerSearchVo).parallelStream()
                        .map(MarkerDto::getVo).collect(Collectors.toList())
        );
    }

    @Operation(summary = "通过ID列表查询点位信息", description = "通过ID列表来进行查询点位信息")
    @PostMapping("/get/list_byid")
    public R<List<MarkerVo>> listMarkerById(@RequestHeader(value = "isTestUser", required = false) String isTestUser, @RequestBody List<Long> markerIdList) {
        return RUtils.create(
                markerService.listMarkerById(markerIdList, StringUtils.hasLength(isTestUser)).parallelStream()
                        .map(MarkerDto::getVo).collect(Collectors.toList())
        );
    }

    @Operation(summary = "分页查询所有点位信息", description = "分页查询所有点位信息")
    @PostMapping("/get/page")
    public R<PageListVo<MarkerVo>> listMarkerPage(@RequestHeader(value = "isTestUser", required = false) String isTestUser, @RequestBody PageSearchVo pageSearchVo) {
        return RUtils.create(
                markerService.listMarkerPage(new PageSearchDto(pageSearchVo), StringUtils.hasLength(isTestUser))
        );
    }

    @Operation(summary = "新增点位（不包括额外字段）", description = "新增完成后返回点位ID")
    @PutMapping("/single")
    public R<Long> createMarker(@RequestBody MarkerSingleVo markerSingleVo) {
        Long newId = markerService.createMarker(new MarkerSingleDto(markerSingleVo));
        cacheService.cleanItemCache();
        cacheService.cleanMarkerCache();
        return RUtils.create(newId);
    }

    @Operation(summary = "新增点位额外字段信息", description = "需保证额外字段的点位都已经添加成功")
    @PutMapping("/extra")
    public R<Boolean> addMarkerExtra(@RequestBody MarkerExtraVo markerExtraVo) {
        Boolean result = markerService.addMarkerExtra(new MarkerExtraDto(markerExtraVo));
        if (result) {
            cacheService.cleanItemCache();
            cacheService.cleanMarkerCache();
        }
        return RUtils.create(result);
    }

    @Operation(summary = "修改点位（不包括额外字段）", description = "根据点位ID修改点位")
    @PostMapping("/single")
    public R<Boolean> updateMarker(@RequestBody MarkerSingleVo markerSingleVo) {
        Boolean result = markerService.updateMarker(new MarkerSingleDto(markerSingleVo));
        cacheService.cleanItemCache();
        cacheService.cleanMarkerCache();
        return RUtils.create(result);
    }

    @Operation(summary = "修改点位额外字段", description = "根据点位ID修改点位额外字段")
    @PostMapping("/extra")
    public R<Boolean> updateMarkerExtra(@RequestBody MarkerExtraVo markerExtraVo) {
        Boolean result = markerService.updateMarkerExtra(new MarkerExtraDto(markerExtraVo));
        if (result) {
            cacheService.cleanItemCache();
            cacheService.cleanMarkerCache();
        }
        return RUtils.create(
                result
        );
    }


    @Operation(summary = "删除点位", description = "根据点位ID列表批量删除点位")
    @DeleteMapping("/{markerId}")
    public R<Boolean> deleteMarker(@PathVariable("markerId") Long markerId) {
        Boolean result = markerService.deleteMarker(markerId);
        cacheService.cleanItemCache();
        cacheService.cleanMarkerCache();
        return RUtils.create(result);
    }

    //////////////END:点位自身的API//////////////

}
