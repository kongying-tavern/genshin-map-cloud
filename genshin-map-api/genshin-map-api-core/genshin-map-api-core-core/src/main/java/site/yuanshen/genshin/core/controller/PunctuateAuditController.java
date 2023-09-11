package site.yuanshen.genshin.core.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import site.yuanshen.common.web.response.R;
import site.yuanshen.common.web.response.RUtils;
import site.yuanshen.data.dto.MarkerPunctuateDto;
import site.yuanshen.data.dto.helper.PageSearchDto;
import site.yuanshen.data.vo.MarkerPunctuateVo;
import site.yuanshen.data.vo.PunctuateSearchVo;
import site.yuanshen.data.vo.helper.PageListVo;
import site.yuanshen.data.vo.helper.PageSearchVo;
import site.yuanshen.genshin.core.service.CacheService;
import site.yuanshen.genshin.core.service.PunctuateAuditService;
import site.yuanshen.genshin.core.service.UserAppenderService;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 打点审核 Controller 层
 *
 * @author Alex Fang
 * @since 2022-10-16
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/punctuate_audit")
@Tag(name = "punctuate_audit", description = "打点审核API")
public class PunctuateAuditController {

    private final PunctuateAuditService punctuateAuditService;
    private final CacheService cacheService;

    //////////////START:审核员的API//////////////

    @Operation(summary = "根据各种条件筛选打点ID",
            description = "支持根据末端地区、末端类型、物品、提交者来进行查询，地区、类型、物品查询不能同时生效，同时存在时报错")
    @PostMapping("/get/id")
    public R<List<Long>> searchPunctuateId(@RequestBody PunctuateSearchVo punctuateSearchVo) {
        return RUtils.create(
                punctuateAuditService.searchPunctuateId(punctuateSearchVo)
        );
    }

    @Operation(summary = "根据各种条件筛选打点信息",
            description = "支持根据末端地区、末端类型、物品、提交者来进行查询，地区、类型、物品查询不能同时生效，同时存在时报错")
    @PostMapping("/get/list_byinfo")
    public R<List<MarkerPunctuateVo>> searchPunctuate(@RequestBody PunctuateSearchVo punctuateSearchVo) {
        R<List<MarkerPunctuateVo>> result = RUtils.create(
                punctuateAuditService.searchPunctuate(punctuateSearchVo).stream()
                        .map(MarkerPunctuateDto::getVo).collect(Collectors.toList())
        );
        UserAppenderService.appendUser(result, result.getData(), true, MarkerPunctuateVo::getCreatorId);
        UserAppenderService.appendUser(result, result.getData(), true, MarkerPunctuateVo::getUpdaterId);
        return result;
    }

    @Operation(summary = "通过打点ID列表查询打点信息", description = "通过打点ID列表查询打点信息")
    @PostMapping("/get/list_byid")
    public R<List<MarkerPunctuateVo>> listPunctuateById(@RequestBody List<Long> punctuateIdList) {
        R<List<MarkerPunctuateVo>> result = RUtils.create(
                punctuateAuditService.listPunctuateById(punctuateIdList).stream()
                        .map(MarkerPunctuateDto::getVo).collect(Collectors.toList())
        );
        UserAppenderService.appendUser(result, result.getData(), true, MarkerPunctuateVo::getCreatorId);
        UserAppenderService.appendUser(result, result.getData(), true, MarkerPunctuateVo::getUpdaterId);
        return result;
    }

    @Operation(summary = "分页查询所有打点信息（包括暂存）", description = "分页查询所有打点信息（包括暂存）")
    @PostMapping("/get/page/all")
    public R<PageListVo<MarkerPunctuateVo>> listAllPunctuatePage(@RequestBody PageSearchVo pageSearchVo) {
        R<PageListVo<MarkerPunctuateVo>> result = RUtils.create(
                punctuateAuditService.listAllPunctuatePage(new PageSearchDto(pageSearchVo))
        );
        UserAppenderService.appendUser(result, result.getData().getRecord(), true, MarkerPunctuateVo::getCreatorId);
        UserAppenderService.appendUser(result, result.getData().getRecord(), true, MarkerPunctuateVo::getUpdaterId);
        return result;
    }

    @Operation(summary = "通过点位审核",
            description = "通过审核，返回点位ID（如果是新建点位，则为新点位ID），通过额外字段关联的点位也会自动通过审核（但不会返回关联点位的ID）")
    @PostMapping("/pass/{punctuateId}")
    public R<Long> passPunctuate(@PathVariable("punctuateId") Long punctuateId) {
        Long passId = punctuateAuditService.passPunctuate(punctuateId);
        cacheService.cleanMarkerCache();
        return RUtils.create(passId);
    }

    @Operation(summary = "驳回点位审核", description = "驳回的点位和通过额外字段关联的点位会回到暂存区")
    @PostMapping("/reject/{punctuateId}")
    public R<Boolean> rejectPunctuate(@PathVariable("punctuateId") Long punctuateId, @RequestBody String auditRemark) {
        return RUtils.create(
                punctuateAuditService.rejectPunctuate(punctuateId,auditRemark)
        );
    }

    @Operation(summary = "删除提交点位", description = "根据提交ID列表来删除提交点位")
    @DeleteMapping("/delete/{punctuateId}")
    public R<Boolean> deletePunctuate(@PathVariable("punctuateId") Long punctuateId) {
        return RUtils.create(
                punctuateAuditService.deletePunctuate(punctuateId)
        );
    }

    //////////////END:审核员的API//////////////
}
