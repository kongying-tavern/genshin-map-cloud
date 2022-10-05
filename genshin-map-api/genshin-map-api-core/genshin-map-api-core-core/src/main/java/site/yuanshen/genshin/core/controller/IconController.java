package site.yuanshen.genshin.core.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import site.yuanshen.common.web.response.R;
import site.yuanshen.common.web.response.RUtils;
import site.yuanshen.data.dto.IconDto;
import site.yuanshen.data.dto.IconSearchDto;
import site.yuanshen.data.dto.IconTypeDto;
import site.yuanshen.data.dto.helper.PageAndTypeListDto;
import site.yuanshen.data.vo.IconSearchVo;
import site.yuanshen.data.vo.IconTypeVo;
import site.yuanshen.data.vo.IconVo;
import site.yuanshen.data.vo.helper.PageAndTypeListVo;
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
@RequestMapping("/icon")
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
                iconService.getIcon(iconId).getVo()
        );
    }

    @Operation(summary = "修改图标信息", description = "由icon_id定位修改一个icon")
    @PostMapping("/update")
    public R<Boolean> updateIcon(@RequestBody IconVo iconVo) {
        return RUtils.create(
                iconService.updateIcon(new IconDto(iconVo))
        );
    }

    @Operation(summary = "新增图标", description = "无需指定icon的id，id由系统自动生成并在响应中返回," +
            "一组name和updater需要唯一（允许单一重复）")
    @PutMapping("/add")
    public R<Long> createIcon(@RequestBody IconVo iconVo) {
        return RUtils.create(
                iconService.createIcon(new IconDto(iconVo))
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

    //////////////START:图标分类的API//////////////

    @Operation(summary = "列出分类", description = "列出图标的分类，parentID为-1的时候为列出所有的根分类，isTraverse为1时遍历所有子分类，默认为1，可分页")
    @PostMapping("/get/type/list")
    public R<PageListVo<IconTypeVo>> listIconType(@RequestBody PageAndTypeListVo searchVo) {
        return RUtils.create(
                iconService.listIconType(new PageAndTypeListDto(searchVo))
        );
    }

    @Operation(summary = "新增分类", description = "类型id在创建后返回")
    @PutMapping("/type")
    public R<Long> addIconType(@RequestBody IconTypeVo iconTypeVo) {
        return RUtils.create(
                iconService.addIconType(new IconTypeDto(iconTypeVo))
        );
    }

    @Operation(summary = "修改分类", description = "由类型ID来定位修改一个分类")
    @PostMapping("/type")
    public R<Boolean> updateIconType(@RequestBody IconTypeVo iconTypeVo) {
        return RUtils.create(
                iconService.updateIconType(new IconTypeDto(iconTypeVo))
        );
    }

    @Operation(summary = "删除分类", description = "这个操作会递归删除，请在前端做二次确认")
    @DeleteMapping("/type/{typeId}")
    public R<Boolean> deleteIconType(@PathVariable("typeId") Long typeId) {
        return RUtils.create(
                iconService.deleteIconType(typeId)
        );
    }

    //////////////END:图标分类的API//////////////

}
