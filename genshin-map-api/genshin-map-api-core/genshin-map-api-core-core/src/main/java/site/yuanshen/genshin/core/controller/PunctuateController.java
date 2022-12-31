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

    @Operation(summary = "分页查询自己提交的未通过的打点信息",
            description = "分页查询自己提交的未通过的打点信息（打点员的API）")
    @PostMapping("/get/page/{authorId}")
    public R<PageListVo<MarkerPunctuateVo>> listSelfPunctuatePage(@RequestBody PageSearchVo pageSearchVo, @PathVariable("authorId") Long authorId, @RequestHeader("userId") Long userId) {
        if (!userId.equals(authorId)) throw new RuntimeException("无权限查看其他人未提交的打点信息");
        return RUtils.create(
                punctuateService.listSelfPunctuatePage(new PageSearchDto(pageSearchVo), authorId)
        );
    }

    @Operation(summary = "提交暂存点位", description = "成功则返回打点提交ID")
    @PutMapping("/")
    public R<Long> addPunctuate(@RequestBody MarkerPunctuateVo punctuateVo, @RequestHeader("userId") Long userId) {
        if (!userId.equals(punctuateVo.getAuthor()))
            throw new RuntimeException("无权限为其他人提交打点信息");
        if (punctuateVo.getOriginalMarkerId()==null && !punctuateVo.getMarkerCreatorId().equals(userId))
            throw new RuntimeException("新增点位时，点位初始标记者需为用户自身");
        return RUtils.create(
                punctuateService.addPunctuate(new MarkerPunctuateDto(punctuateVo))
        );
    }

    @Operation(summary = "将暂存点位提交审核", description = "将暂存点位提交审核")
    @PutMapping("/push/{authorId}")
    public R<Boolean> pushPunctuate(@PathVariable("authorId") Long authorId, @RequestHeader("userId") Long userId) {
        if (!userId.equals(authorId)) throw new RuntimeException("无权限将其他用户的暂存点位提交审核");
        return RUtils.create(
                punctuateService.pushPunctuate(authorId)
        );
    }

    @Operation(summary = "修改自身未提交的暂存点位", description = "根据点位ID修改点位")
    @PostMapping("/")
    public R<Boolean> updateSelfPunctuate(@RequestBody MarkerPunctuateVo markerPunctuateVo, @RequestHeader("userId") Long userId) {
        if (!userId.equals(markerPunctuateVo.getAuthor())) throw new RuntimeException("无权限修改其他人未提交的打点信息");
        return RUtils.create(
                punctuateService.updateSelfPunctuate(new MarkerPunctuateDto(markerPunctuateVo))
        );
    }

    @Operation(summary = "删除自己未通过的提交点位", description = "根据提交ID列表来删除提交点位，会对打点员ID进行校验")
    @DeleteMapping("/delete/{authorId}/{punctuateId}")
    public R<Boolean> deleteSelfPunctuate(@PathVariable("punctuateId") Long punctuateId, @PathVariable("authorId") Long authorId, @RequestHeader("userId") Long userId) {
        if (!userId.equals(authorId)) throw new RuntimeException("无权限删除其他用户的暂存点位");
        return RUtils.create(
                punctuateService.deleteSelfPunctuate(punctuateId, authorId)
        );
    }

    //////////////END:打点员的API//////////////

}
