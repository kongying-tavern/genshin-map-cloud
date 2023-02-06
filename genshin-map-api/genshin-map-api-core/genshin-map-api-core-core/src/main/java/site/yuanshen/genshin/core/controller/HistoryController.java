package site.yuanshen.genshin.core.controller;


import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import site.yuanshen.common.web.response.R;
import site.yuanshen.common.web.response.RUtils;
import site.yuanshen.data.dto.HistorySearchDto;
import site.yuanshen.data.dto.ItemSearchDto;
import site.yuanshen.data.entity.History;
import site.yuanshen.data.vo.HistorySearchVo;
import site.yuanshen.data.vo.HistoryVo;
import site.yuanshen.data.vo.ItemSearchVo;
import site.yuanshen.data.vo.ItemVo;
import site.yuanshen.data.vo.helper.PageListVo;
import site.yuanshen.genshin.core.manager.HistoryManager;
import site.yuanshen.genshin.core.service.HistoryService;

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
    private final HistoryManager historyManager;


    /**
     * 历史记录分页
     *
     * @return R
     */

//    @PostMapping("/get/page")
//    public R<Page<History>> listHistory(Page<History> page, Integer type, Long id) {
//        return RUtils.create(historyService.page(page, Wrappers.<History>lambdaQuery().eq(History::getType, type).eq(History::getTId, id)));
//    }


    @Operation(summary = "历史记录分页", description = "历史记录分页")
    @PostMapping("/get/list")
    public R<PageListVo<HistoryVo>> getList(@RequestBody HistorySearchVo historySearchVo) {
        return RUtils.create(
                historyService.listPage(new HistorySearchDto(historySearchVo))
        );
    }

    /**
     * 回滚记录
     *
     * @return R
     */
    @Operation(summary = "回滚记录", description = "回滚记录")
    @PostMapping("/rollback")
    public R<Boolean> rollback(Long id) {
        return RUtils.create(historyManager.rollback(id));
    }

}
