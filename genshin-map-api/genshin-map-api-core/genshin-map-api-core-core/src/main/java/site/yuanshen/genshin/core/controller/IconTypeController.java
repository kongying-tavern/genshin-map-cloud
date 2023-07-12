package site.yuanshen.genshin.core.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import site.yuanshen.common.web.response.R;
import site.yuanshen.common.web.response.RUtils;
import site.yuanshen.data.dto.IconTypeDto;
import site.yuanshen.data.dto.helper.PageAndTypeSearchDto;
import site.yuanshen.data.vo.IconTypeVo;
import site.yuanshen.data.vo.helper.PageAndTypeSearchVo;
import site.yuanshen.data.vo.helper.PageListVo;
import site.yuanshen.genshin.core.service.IconTypeService;
import site.yuanshen.genshin.core.service.UserAppenderService;

/**
 * 图标库类别 Controller 层
 *
 * @author Alex
 * @since 2022-06-02
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/icon_type")
@Tag(name = "icon_type", description = "图标分类API")
public class IconTypeController {

    private final IconTypeService iconTypeService;

    //////////////START:图标分类的API//////////////

    @Operation(summary = "列出分类", description = "列出图标的分类，parentID为-1的时候为列出所有的根分类，isTraverse为1时遍历所有子分类，默认为1，可分页")
    @PostMapping("/get/list")
    public R<PageListVo<IconTypeVo>> listIconType(@RequestBody PageAndTypeSearchVo searchVo) {
        R<PageListVo<IconTypeVo>> result = RUtils.create(
                iconTypeService.listIconType(new PageAndTypeSearchDto(searchVo))
        );
        UserAppenderService.appendUser(result, result.getData().getRecord(), true, IconTypeVo::getCreatorId);
        UserAppenderService.appendUser(result, result.getData().getRecord(), true, IconTypeVo::getUpdaterId);
        return result;
    }

    @Operation(summary = "新增分类", description = "类型id在创建后返回")
    @PutMapping("/add")
    public R<Long> addIconType(@RequestBody IconTypeVo iconTypeVo) {
        return RUtils.create(
                iconTypeService.addIconType(new IconTypeDto(iconTypeVo))
        );
    }

    @Operation(summary = "修改分类", description = "由类型ID来定位修改一个分类")
    @PostMapping("/update")
    public R<Boolean> updateIconType(@RequestBody IconTypeVo iconTypeVo) {
        return RUtils.create(
                iconTypeService.updateIconType(new IconTypeDto(iconTypeVo))
        );
    }

    @Operation(summary = "删除分类", description = "这个操作会递归删除，请在前端做二次确认")
    @DeleteMapping("/delete/{typeId}")
    public R<Boolean> deleteIconType(@PathVariable("typeId") Long typeId) {
        return RUtils.create(
                iconTypeService.deleteIconType(typeId)
        );
    }

    //////////////END:图标分类的API//////////////

}
