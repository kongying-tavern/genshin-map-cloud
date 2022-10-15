package site.yuanshen.genshin.core.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import site.yuanshen.common.web.response.R;
import site.yuanshen.common.web.response.RUtils;
import site.yuanshen.data.dto.*;
import site.yuanshen.data.dto.helper.PageSearchDto;
import site.yuanshen.data.vo.*;
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

    //////////////START:打点员的API//////////////

    @Operation(summary = "分页查询所有打点信息", description = "分页查询所有打点信息")
    @PostMapping("/punctuate/author/get/page")
    public R<PageListVo<MarkerPunctuateVo>> listPunctuatePage(@RequestBody PageSearchVo pageSearchVo) {
        return RUtils.create(
                markerService.listPunctuatePage(new PageSearchDto(pageSearchVo))
        );
    }

    @Operation(summary = "分页查询自己提交的未通过的打点信息（不包含额外字段）",
            description = "分页查询自己提交的未通过的打点信息（不包含额外字段）（打点员的API）")
    @PostMapping("/punctuate/author/get/page_single/{authorId}")
    public R<PageListVo<MarkerSinglePunctuateVo>> listSelfSinglePunctuatePage(@RequestBody PageSearchVo pageSearchVo, @PathVariable("authorId") Long authorId) {
        return RUtils.create(
                markerService.listSelfSinglePunctuatePage(new PageSearchDto(pageSearchVo), authorId)
        );
    }

    @Operation(summary = "分页查询自己提交的未通过的打点信息（只包含额外字段）",
            description = "分页查询自己提交的未通过的打点信息（只包含额外字段） （打点员的API）")
    @PostMapping("/punctuate/author/get/page_extra/{authorId}")
    public R<PageListVo<MarkerExtraPunctuateVo>> listSelfExtraPunctuatePage(@RequestBody PageSearchVo pageSearchVo, @PathVariable("authorId") Long authorId) {
        return RUtils.create(
                markerService.listSelfExtraPunctuatePage(new PageSearchDto(pageSearchVo), authorId)
        );
    }

    @Operation(summary = "提交暂存点位（不含额外字段）", description = "成功则返回打点提交ID")
    @PutMapping("/punctuate/author/single")
    public R<Long> addSinglePunctuate(@RequestBody MarkerSinglePunctuateVo markerSinglePunctuateVo) {
        return RUtils.create(
                markerService.addSinglePunctuate(new MarkerSinglePunctuateDto(markerSinglePunctuateVo))
        );
    }

    @Operation(summary = "提交暂存点位额外字段", description = "在涉及的所有点位已经暂存后在使用该api")
    @PutMapping("/punctuate/author/extra")
    public R<Boolean> addExtraPunctuate(@RequestBody MarkerExtraPunctuateVo markerExtraPunctuateVo) {
        return RUtils.create(
                markerService.addExtraPunctuate(new MarkerExtraPunctuateDto(markerExtraPunctuateVo))
        );
    }

    @Operation(summary = "将暂存点位提交审核", description = "将暂存点位提交审核")
    @PutMapping("/punctuate/author/push/{authorId}")
    public R<Boolean> pushPunctuate(@PathVariable("authorId") Long authorId) {
        return RUtils.create(
                markerService.pushPunctuate(authorId)
        );
    }

    @Operation(summary = "修改自身未提交的暂存点位（不包括额外字段）", description = "根据点位ID修改点位")
    @PostMapping("/punctuate/author/single")
    public R<Boolean> updateSelfSinglePunctuate(@RequestBody MarkerSinglePunctuateVo singlePunctuateVo) {
        return RUtils.create(
                markerService.updateSelfSinglePunctuate(new MarkerSinglePunctuateDto(singlePunctuateVo))
        );
    }

    @Operation(summary = "修改自身未提交的暂存点位的额外字段", description = "根据点位ID修改点位")
    @PostMapping("/punctuate/author/extra")
    public R<Boolean> updateSelfPunctuateExtra(@RequestBody MarkerExtraPunctuateVo extraPunctuateVo) {
        return RUtils.create(
                markerService.updateSelfPunctuateExtra(new MarkerExtraPunctuateDto(extraPunctuateVo))
        );
    }

    @Operation(summary = "删除自己未通过的提交点位", description = "根据提交ID列表来删除提交点位，会对打点员ID进行校验")
    @DeleteMapping("/punctuate/author/{authorId}/{punctuateId}")
    public R<Boolean> deleteSelfPunctuate(@PathVariable("punctuateId") Long punctuateId, @PathVariable("authorId") Long authorId) {
        return RUtils.create(
                markerService.deleteSelfPunctuate(punctuateId, authorId)
        );
    }

    //////////////END:打点员的API//////////////


}
