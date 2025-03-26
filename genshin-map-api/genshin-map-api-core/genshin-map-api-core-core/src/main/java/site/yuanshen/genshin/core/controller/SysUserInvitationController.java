package site.yuanshen.genshin.core.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import site.yuanshen.common.web.response.R;
import site.yuanshen.common.web.response.RUtils;
import site.yuanshen.data.dto.SysUserInvitationConsumeDto;
import site.yuanshen.data.dto.SysUserInvitationDto;
import site.yuanshen.data.dto.SysUserInvitationSearchDto;
import site.yuanshen.data.vo.*;
import site.yuanshen.data.vo.helper.PageListVo;
import site.yuanshen.genshin.core.service.SysUserInvitationService;
import site.yuanshen.genshin.core.service.UserAppenderService;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/system/invitation")
@Tag(name = "invitation", description = "用户邀请API")
public class SysUserInvitationController {
    private final SysUserInvitationService userInvitationService;

    @Operation(summary = "列出用户邀请", description = "列出用户邀请")
    @PostMapping("/list")
    public R<PageListVo<SysUserInvitationVo>> listInvitation(@RequestBody SysUserInvitationSearchVo searchVo) {
        R<PageListVo<SysUserInvitationVo>> result = RUtils.create(
                userInvitationService.searchInvitationPage(new SysUserInvitationSearchDto(searchVo))
        );
        UserAppenderService.appendUser(result, result.getData().getRecord(), true, SysUserInvitationVo::getCreatorId);
        UserAppenderService.appendUser(result, result.getData().getRecord(), true, SysUserInvitationVo::getUpdaterId);
        return result;
    }

    @Operation(summary = "新增/更新用户邀请", description = "新增/更新用户邀请")
    @PostMapping("/update")
    public R<SysUserInvitationSmallVo> updateInvitation(@RequestBody SysUserInvitationVo invitationVo) {
        R<SysUserInvitationSmallVo> result = RUtils.create(
                userInvitationService.updateInvitation(new SysUserInvitationDto(invitationVo))
        );
        return result;
    }

    @Operation(summary = "检查用户邀请数据", description = "检查用户邀请数据")
    @PostMapping("/info")
    public R<SysUserInvitationSmallVo> checkInvitation(@RequestBody SysUserInvitationSmallVo invitationVo) {
        R<SysUserInvitationSmallVo> result = RUtils.create(
                userInvitationService.checkInvitation(invitationVo)
        );
        return result;
    }

    @Operation(summary = "检查用户邀请数据", description = "检查用户邀请数据")
    @PostMapping("/consume")
    public R<SysUserInvitationConsumeResultVo> consumeInvitation(@RequestBody SysUserInvitationConsumeVo consumeInvitationVo) {
        R<SysUserInvitationConsumeResultVo> result = RUtils.create(
                userInvitationService.consumeInvitation(new SysUserInvitationConsumeDto(consumeInvitationVo))
        );
        return result;
    }

    @Operation(summary = "删除用户邀请", description = "删除用户邀请")
    @DeleteMapping("/{invitationId}")
    public R<Boolean> deleteInvitation(@PathVariable("invitationId") Long invitationId) {
        return RUtils.create(
                userInvitationService.deleteInvitation(invitationId)
        );
    }
}
