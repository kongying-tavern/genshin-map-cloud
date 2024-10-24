package site.yuanshen.genshin.core.controller;


import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import site.yuanshen.common.web.response.R;
import site.yuanshen.common.web.response.RUtils;
import site.yuanshen.data.dto.AreaDto;
import site.yuanshen.data.enums.HiddenFlagEnum;
import site.yuanshen.data.vo.AreaSearchVo;
import site.yuanshen.data.vo.AreaVo;
import site.yuanshen.genshin.core.service.AreaService;
import site.yuanshen.genshin.core.service.UserAppenderService;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 地区 Controller 层
 *
 * @author Moment
 * @since 2022-06-11
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/area")
@Tag(name = "area", description = "地区API")
public class AreaController {

    private final AreaService areaService;

    @Operation(summary = "列出地区", description = "可根据父级地区id列出子地区列表")
    @PostMapping("/get/list")
    public R<List<AreaVo>> listArea(@Schema(hidden = true)  @RequestHeader(value = "userDataLevel",required = false) String userDataLevel, @RequestBody AreaSearchVo areaSearchVo) {
        //todo userDataLevel应该作为参数传入，vo作为前端传值不应该加userDataLevel
        areaSearchVo.setHiddenFlagList(HiddenFlagEnum.getFlagListByMask(userDataLevel));
        R<List<AreaVo>> result = RUtils.create(
                areaService.listArea(areaSearchVo)
                        .stream().map(AreaDto::getVo).collect(Collectors.toList())
        );
        UserAppenderService.appendUser(result, result.getData(), true, AreaVo::getCreatorId);
        UserAppenderService.appendUser(result, result.getData(), true, AreaVo::getUpdaterId);
        return result;
    }

    @Operation(summary = "获取单个地区信息", description = "获取单个地区信息")
    @PostMapping("/get/{areaId}")
    public R<AreaVo> getArea(@Parameter(hidden = true) @RequestHeader(value = "userDataLevel",required = false) String userDataLevel, @PathVariable("areaId") Long areaId) {
        R<AreaVo> result = RUtils.create(
                areaService.getArea(areaId, HiddenFlagEnum.getFlagListByMask(userDataLevel)).getVo()
        );
        UserAppenderService.appendUser(result, result.getData(), false, AreaVo::getCreatorId);
        UserAppenderService.appendUser(result, result.getData(), false, AreaVo::getUpdaterId);
        return result;
    }

    @Operation(summary = "新增地区", description = "返回新增地区ID")
    @PutMapping("/add")
    public R<Long> createArea(@RequestBody AreaVo areaVo) {
        return RUtils.create(
                areaService.createArea(new AreaDto(areaVo))
        );
    }

    @Operation(summary = "修改地区", description = "修改地区")
    @PostMapping("/update")
    public R<Boolean> updateArea(@RequestBody AreaVo areaVo) {
        return RUtils.create(
                areaService.updateArea(new AreaDto(areaVo))
        );
    }

    @Operation(summary = "删除地区", description = "此操作会递归删除，请在前端做二次确认")
    @DeleteMapping("/{areaId}")
    public R<Boolean> deleteArea(@PathVariable("areaId") Long areaId) {
        return RUtils.create(
                areaService.deleteArea(areaId)
        );
    }


}
