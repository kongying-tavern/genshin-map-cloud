package site.yuanshen.genshin.core.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import site.yuanshen.common.web.response.R;
import site.yuanshen.common.web.response.RUtils;
import site.yuanshen.data.dto.SysUserInvitationSearchDto;
import site.yuanshen.data.vo.SysUserInvitationSearchVo;
import site.yuanshen.data.vo.SysUserInvitationVo;
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
                userInvitationService.searchInvitation(new SysUserInvitationSearchDto(searchVo))
        );
        UserAppenderService.appendUser(result, result.getData().getRecord(), true, SysUserInvitationVo::getCreatorId);
        UserAppenderService.appendUser(result, result.getData().getRecord(), true, SysUserInvitationVo::getUpdaterId);
        return result;
    }
}
