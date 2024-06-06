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
import site.yuanshen.data.dto.SysActionLogSearchDto;
import site.yuanshen.data.vo.SysActionLogSearchVo;
import site.yuanshen.data.vo.SysActionLogVo;
import site.yuanshen.data.vo.helper.PageListVo;
import site.yuanshen.genshin.core.service.SysActionLogService;
import site.yuanshen.genshin.core.service.UserAppenderService;

/**
 * 操作日志API
 *
 * @author Alex Fang
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/system/action_log")
@Tag(name = "action_log", description = "操作日志API")
public class SysActionLogController {

    private final SysActionLogService sysActionLogService;

    @Operation(summary = "历史记录分页", description = "历史记录分页")
    @PostMapping("/list")
    public R<PageListVo<SysActionLogVo>> searchActionLog(@RequestBody SysActionLogSearchVo historySearchVo) {
        R<PageListVo<SysActionLogVo>> result = RUtils.create(
                sysActionLogService.listPage(new SysActionLogSearchDto(historySearchVo))
        );
        UserAppenderService.appendUser(result, result.getData().getRecord(), true, SysActionLogVo::getCreatorId);
        UserAppenderService.appendUser(result, result.getData().getRecord(), true, SysActionLogVo::getUpdaterId);
        return result;
    }


}
