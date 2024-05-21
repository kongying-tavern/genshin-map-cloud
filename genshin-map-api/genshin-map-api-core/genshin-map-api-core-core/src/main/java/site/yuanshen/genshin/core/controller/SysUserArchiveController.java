package site.yuanshen.genshin.core.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import site.yuanshen.common.web.response.R;
import site.yuanshen.common.web.response.RUtils;
import site.yuanshen.data.vo.SysArchiveSlotVo;
import site.yuanshen.data.vo.SysArchiveVo;
import site.yuanshen.genshin.core.service.SysUserArchiveService;

import java.util.List;

/**
 * 用户存档管理API
 *
 * @author Moment
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/system/archive")
@Tag(name = "archive", description = "用户存档管理API")
public class SysUserArchiveController {

    private final SysUserArchiveService archiveService;

    @Operation(summary = "获取指定存档槽位的当前存档", description = "获取指定存档槽位的当前存档，获取槽位最新存档（1号历史记录的存档）")
    @GetMapping("/last/{slot_index}")
    public R<SysArchiveVo> getLastArchive(@PathVariable("slot_index") int slotIndex, @Parameter(hidden = true) @RequestHeader("userId") Long userId) {
        return RUtils.create(archiveService.getLastArchive(slotIndex, userId));
    }

    @Operation(summary = "获取指定槽位的所有历史存档", description = "获取指定槽位的所有历史存档")
    @GetMapping("/history/{slot_index}")
    public R<SysArchiveSlotVo> getHistoryArchive(@PathVariable("slot_index") int slotIndex, @Parameter(hidden = true) @RequestHeader("userId") Long userId) {
        return RUtils.create(archiveService.getSlot(slotIndex, userId));
    }

    @Operation(summary = "获取所有槽位的历史存档", description = "获取所有槽位的历史存档")
    @GetMapping("/all_history")
    public R<List<SysArchiveSlotVo>> getAllHistoryArchive(@Parameter(hidden = true) @RequestHeader("userId") Long userId) {
        return RUtils.create(archiveService.getAllSlot(userId));
    }

    @Operation(summary = "新建存档槽位并将存档存入",
            description = "新建存档并存入，注意槽位下标不能冲突")
    @PutMapping("/{slot_index}/{name}")
    public R<Boolean> createSlotAndSaveArchive(@PathVariable("slot_index") int slotIndex, @RequestBody String archive, @Parameter(hidden = true) @RequestHeader("userId") Long userId, @PathVariable("name") String name) {

        return RUtils.create(archiveService.createSlotAndSaveArchive(slotIndex, archive, userId, name));
    }

    @Operation(summary = "存档入指定槽位",
            description = "指定槽位下标，将存档存入该槽位。如果存档与最后一次一致，则不存入，并返回false；如果槽位已满，则挤掉最后一次备份。")
    @PostMapping("/save/{slot_index}")
    public R<Boolean> saveArchive(@PathVariable("slot_index") int slotIndex, @RequestBody String archive, @Parameter(hidden = true) @RequestHeader("userId") Long userId) {
        return RUtils.create(archiveService.saveArchive(slotIndex, archive, userId));
    }

    @Operation(summary = "重命名指定槽位",
            description = "重命名槽位，后端会对槽位校验，更新失败返回false")
    @PostMapping("/rename/{slot_index}/{new_name}")
    public R<Boolean> renameSlot(@PathVariable("slot_index") int slotIndex, @PathVariable("new_name") String newName, @Parameter(hidden = true) @RequestHeader("userId") Long userId) {
        return RUtils.create(archiveService.renameSlot(slotIndex, userId, newName));
    }



    @Operation(summary = "删除最近一次存档（恢复为上次存档）",
            description = "删除最近一次存档，也意味着恢复为上次存档。会返回上一次存档。如果存档为空，则返回400，并附带报错信息")
    @DeleteMapping("/restore/{slot_index}")
    public R<SysArchiveVo> restoreArchive(@PathVariable("slot_index") int slotIndex, @Parameter(hidden = true) @RequestHeader("userId") Long userId) {
        return RUtils.create(archiveService.restoreArchive(slotIndex, userId));
    }

    @Operation(summary = "删除存档槽位", description = "将整个存档槽位删除")
    @DeleteMapping("/slot/{slot_index}")
    public R<Boolean> removeArchive(@PathVariable("slot_index") int slotIndex, @Parameter(hidden = true) @RequestHeader("userId") Long userId) {
        return RUtils.create(archiveService.removeArchive(slotIndex, userId));
    }

}
