package site.yuanshen.genshin.core.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import site.yuanshen.common.web.response.R;
import site.yuanshen.common.web.response.RUtils;
import site.yuanshen.data.dto.MarkerExtraPunctuateDto;
import site.yuanshen.data.dto.MarkerSinglePunctuateDto;
import site.yuanshen.data.dto.helper.PageSearchDto;
import site.yuanshen.data.vo.MarkerExtraPunctuateVo;
import site.yuanshen.data.vo.MarkerPunctuateVo;
import site.yuanshen.data.vo.MarkerSinglePunctuateVo;
import site.yuanshen.data.vo.helper.PageListVo;
import site.yuanshen.data.vo.helper.PageSearchVo;
import site.yuanshen.genshin.core.service.PunctuateService;

/**
 * 打点 Controller 层
 *
 * @author Alex Fang
 * @since 2022-10-16
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/punctuate")
@Tag(name = "punctuate", description = "打点API")
public class PunctuateController {

    private final PunctuateService punctuateService;

    //////////////START:打点员的API//////////////

    @Operation(summary = "分页查询所有打点信息", description = "分页查询所有打点信息")
    @PostMapping("/get/page")
    public R<PageListVo<MarkerPunctuateVo>> listPunctuatePage(@RequestBody PageSearchVo pageSearchVo) {
        return RUtils.create(
                punctuateService.listPunctuatePage(new PageSearchDto(pageSearchVo))
        );
    }

    @Operation(summary = "分页查询自己提交的未通过的打点信息（不包含额外字段）",
            description = "分页查询自己提交的未通过的打点信息（不包含额外字段）（打点员的API）")
    @PostMapping("/get/page_single/{authorId}")
    public R<PageListVo<MarkerSinglePunctuateVo>> listSelfSinglePunctuatePage(@RequestBody PageSearchVo pageSearchVo, @PathVariable("authorId") Long authorId) {
        return RUtils.create(
                punctuateService.listSelfSinglePunctuatePage(new PageSearchDto(pageSearchVo), authorId)
        );
    }

    @Operation(summary = "分页查询自己提交的未通过的打点信息（只包含额外字段）",
            description = "分页查询自己提交的未通过的打点信息（只包含额外字段） （打点员的API）")
    @PostMapping("/get/page_extra/{authorId}")
    public R<PageListVo<MarkerExtraPunctuateVo>> listSelfExtraPunctuatePage(@RequestBody PageSearchVo pageSearchVo, @PathVariable("authorId") Long authorId) {
        return RUtils.create(
                punctuateService.listSelfExtraPunctuatePage(new PageSearchDto(pageSearchVo), authorId)
        );
    }

    @Operation(summary = "提交暂存点位（不含额外字段）", description = "成功则返回打点提交ID")
    @PutMapping("/single")
    public R<Long> addSinglePunctuate(@RequestBody MarkerSinglePunctuateVo markerSinglePunctuateVo) {
        return RUtils.create(
                punctuateService.addSinglePunctuate(new MarkerSinglePunctuateDto(markerSinglePunctuateVo))
        );
    }

    @Operation(summary = "提交暂存点位额外字段", description = "在涉及的所有点位已经暂存后在使用该api")
    @PutMapping("/extra")
    public R<Boolean> addExtraPunctuate(@RequestBody MarkerExtraPunctuateVo markerExtraPunctuateVo) {
        return RUtils.create(
                punctuateService.addExtraPunctuate(new MarkerExtraPunctuateDto(markerExtraPunctuateVo))
        );
    }

    @Operation(summary = "将暂存点位提交审核", description = "将暂存点位提交审核")
    @PutMapping("/push/{authorId}")
    public R<Boolean> pushPunctuate(@PathVariable("authorId") Long authorId) {
        return RUtils.create(
                punctuateService.pushPunctuate(authorId)
        );
    }

    @Operation(summary = "修改自身未提交的暂存点位（不包括额外字段）", description = "根据点位ID修改点位")
    @PostMapping("/single")
    public R<Boolean> updateSelfSinglePunctuate(@RequestBody MarkerSinglePunctuateVo singlePunctuateVo) {
        return RUtils.create(
                punctuateService.updateSelfSinglePunctuate(new MarkerSinglePunctuateDto(singlePunctuateVo))
        );
    }

    @Operation(summary = "修改自身未提交的暂存点位的额外字段", description = "根据点位ID修改点位")
    @PostMapping("/extra")
    public R<Boolean> updateSelfPunctuateExtra(@RequestBody MarkerExtraPunctuateVo extraPunctuateVo) {
        return RUtils.create(
                punctuateService.updateSelfPunctuateExtra(new MarkerExtraPunctuateDto(extraPunctuateVo))
        );
    }

    @Operation(summary = "删除自己未通过的提交点位", description = "根据提交ID列表来删除提交点位，会对打点员ID进行校验")
    @DeleteMapping("/delete/{authorId}/{punctuateId}")
    public R<Boolean> deleteSelfPunctuate(@PathVariable("punctuateId") Long punctuateId, @PathVariable("authorId") Long authorId) {
        return RUtils.create(
                punctuateService.deleteSelfPunctuate(punctuateId, authorId)
        );
    }

    //////////////END:打点员的API//////////////

}
