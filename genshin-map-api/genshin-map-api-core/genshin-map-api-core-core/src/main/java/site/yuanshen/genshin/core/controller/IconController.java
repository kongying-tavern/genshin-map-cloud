package site.yuanshen.genshin.core.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import site.yuanshen.common.web.response.R;
import site.yuanshen.common.web.response.RUtils;
import site.yuanshen.data.dto.IconSearchDto;
import site.yuanshen.data.vo.IconSearchVo;
import site.yuanshen.data.vo.IconVo;
import site.yuanshen.data.vo.helper.PageListVo;
import site.yuanshen.genshin.core.service.IconService;

/**
 * 图标库 Controller 层
 *
 * @author Moment
 * @since 2022-06-02
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/icon")
@Tag(name = "icon", description = "图标API")
public class IconController {

    private final IconService iconService;

    //////////////START:图标本身的API//////////////

    @Operation(summary = "列出图标", description = "可按照分类和上传者进行查询，也可根据ID批量查询，可分页")
    @PostMapping("/get/list")
    public R<PageListVo<IconVo>> listIcon(@RequestBody IconSearchVo iconSearchVo) {
        return RUtils.create(
                iconService.listIcon(new IconSearchDto(iconSearchVo))
        );
    }

    @Operation(summary = "获取单个图标信息", description = "获取单个图标信息")
    @PostMapping("/get/single/{iconId}")
    public R<IconVo> getIcon(@PathVariable("iconId") Long iconId) {
        return RUtils.create(
                iconService.getIcon(iconId)
        );
    }

    @Operation(summary = "修改图标信息", description = "由icon_id定位修改一个icon")
    @PostMapping("/update")
    public R<Boolean> updateIcon(@RequestBody IconVo iconVo) {
        return RUtils.create(
                iconService.updateIcon(iconVo)
        );
    }

    @Operation(summary = "新增图标", description = "无需指定icon的id，id由系统自动生成并在响应中返回," +
            "一组name和updater需要唯一（允许单一重复）")
    @PutMapping("/add")
    public R<Long> createIcon(@RequestBody IconVo iconVo) {
        return RUtils.create(
                iconService.createIcon(iconVo)
        );
    }

    @Operation(summary = "删除图标", description = "删除图标")
    @DeleteMapping("/delete/{iconId}")
    public R<Boolean> deleteIcon(@PathVariable("iconId") Long iconId) {
        return RUtils.create(
                iconService.deleteIcon(iconId)
        );
    }

    //////////////END:图标本身的API//////////////
}
