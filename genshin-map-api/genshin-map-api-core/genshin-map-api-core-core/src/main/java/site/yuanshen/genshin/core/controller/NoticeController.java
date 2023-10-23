package site.yuanshen.genshin.core.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import site.yuanshen.common.web.response.R;
import site.yuanshen.common.web.response.RUtils;
import site.yuanshen.data.dto.NoticeDto;
import site.yuanshen.data.dto.NoticeSearchDto;
import site.yuanshen.data.vo.NoticeSearchVo;
import site.yuanshen.data.vo.NoticeVo;
import site.yuanshen.data.vo.helper.PageListVo;
import site.yuanshen.genshin.core.service.NoticeService;
import site.yuanshen.genshin.core.service.UserAppenderService;

/**
 * 公告 Controller 层
 *
 * @author Alex Fang
 * @since 2023-08-06
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/notice")
@Tag(name = "notice", description = "公告API")
public class NoticeController {
    private final NoticeService noticeService;

    @Operation(summary = "分页查询所有公告信息", description = "分页查询所有点位信息")
    @PostMapping("/get/list")
    public R<PageListVo<NoticeVo>> listNotice(@RequestBody NoticeSearchVo noticeSearchVo) {
        R<PageListVo<NoticeVo>> result = RUtils.create(
                noticeService.listNotice(new NoticeSearchDto(noticeSearchVo))
        );
        UserAppenderService.appendUser(result, result.getData().getRecord(), true, NoticeVo::getCreatorId);
        UserAppenderService.appendUser(result, result.getData().getRecord(), true, NoticeVo::getUpdaterId);
        return result;
    }

    @Operation(summary = "修改公告", description = "修改公告")
    @PostMapping("/update")
    public R<Boolean> updateNotice(@RequestBody NoticeVo noticeVo) {
        return RUtils.create(
            noticeService.updateNotice(new NoticeDto(noticeVo))
        );
    }

    @Operation(summary = "新增公告", description = "返回新增公告ID")
    @PutMapping("/add")
    public R<Long> createNotice(@RequestBody NoticeVo noticeVo) {
        return RUtils.create(
                noticeService.createNotice(new NoticeDto(noticeVo))
        );
    }

    @Operation(summary = "删除公告", description = "删除公告，请在前端做二次确认")
    @DeleteMapping("/{noticeId}")
    public R<Boolean> deleteNotice(@PathVariable("noticeId") Long noticeId) {
        return RUtils.create(
                noticeService.deleteNotice(noticeId)
        );
    }
}