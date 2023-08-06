package site.yuanshen.genshin.core.controller;


import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;
import site.yuanshen.common.web.response.R;
import site.yuanshen.common.web.response.RUtils;
import site.yuanshen.data.dto.HistorySearchDto;
import site.yuanshen.data.vo.HistorySearchVo;
import site.yuanshen.data.vo.HistoryVo;
import site.yuanshen.data.vo.helper.PageListVo;
import site.yuanshen.genshin.core.service.HistoryService;
import site.yuanshen.genshin.core.service.UserAppenderService;

/**
 * 历史记录
 *
 * @author :  Hu
 */
@RestController
@AllArgsConstructor
@RequestMapping("/api/history")
@Tag(name = "history", description = "历史记录API")
public class HistoryController {
    private final HistoryService historyService;

    @Operation(summary = "历史记录分页", description = "历史记录分页")
    @PostMapping("/get/list")
    public R<PageListVo<HistoryVo>> getList(@RequestBody HistorySearchVo historySearchVo) {
        R<PageListVo<HistoryVo>> result = RUtils.create(
                historyService.listPage(new HistorySearchDto(historySearchVo))
        );
        UserAppenderService.appendUser(result, result.getData().getRecord(), true, HistoryVo::getCreatorId);
        UserAppenderService.appendUser(result, result.getData().getRecord(), true, HistoryVo::getUpdaterId);
        return result;
    }

}
