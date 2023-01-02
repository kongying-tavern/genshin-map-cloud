package site.yuanshen.genshin.core.controller;

import com.alibaba.fastjson.JSON;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import site.yuanshen.common.web.response.R;
import site.yuanshen.common.web.response.RUtils;
import site.yuanshen.data.dto.RouteDto;
import site.yuanshen.data.dto.helper.PageSearchDto;
import site.yuanshen.data.enums.HiddenFlagEnum;
import site.yuanshen.data.enums.RoleEnum;
import site.yuanshen.data.vo.RouteSearchVo;
import site.yuanshen.data.vo.RouteVo;
import site.yuanshen.data.vo.helper.PageListVo;
import site.yuanshen.data.vo.helper.PageSearchVo;
import site.yuanshen.genshin.core.service.RouteService;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 路线 Controller 层
 *
 * @author Moment
 * @since 2023-01-01
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/route")
@Tag(name = "route", description = "路线API")
public class RouteController {

    private final RouteService routeService;

    @Operation(summary = "分页查询所有路线信息", description = "分页查询所有路线信息，会根据当前角色决定不同的显隐等级")
    @PostMapping("/get/page")
    public R<PageListVo<RouteVo>> listRoutePage(@RequestBody PageSearchVo pageSearchVo, @RequestHeader(value = "userDataLevel", required = false) String userDataLevel) {
        return RUtils.create(
                routeService.listRoutePage(new PageSearchDto(pageSearchVo),HiddenFlagEnum.getFlagList(userDataLevel))
        );
    }

    @Operation(summary = "根据条件筛选分页查询路线信息", description = "根据条件筛选分页查询路线信息，会根据当前角色决定不同的显隐等级")
    @PostMapping("/get/search")
    public R<PageListVo<RouteVo>> listRoutePageSearch(@RequestBody RouteSearchVo searchVo, @RequestHeader(value = "userDataLevel", required = false) String userDataLevel) {
        return RUtils.create(
                routeService.listRoutePageSearch(searchVo, HiddenFlagEnum.getFlagList(userDataLevel))
        );
    }

    @Operation(summary = "根据id列表查询路线信息", description = "根据id列表查询路线信息，会根据当前角色决定不同的显隐等级")
    @PostMapping("/get/list_byid")
    public R<List<RouteVo>> listRouteById(@RequestBody List<Long> idList, @RequestHeader(value = "userDataLevel", required = false) String userDataLevel) {
        return RUtils.create(
                routeService.listRouteById(idList, HiddenFlagEnum.getFlagList(userDataLevel))
                        .parallelStream().map(RouteDto::getVo).collect(Collectors.toList())
        );
    }

    @Operation(summary = "新增路线", description = "返回新增路线ID")
    @PutMapping("/add")
    public R<Long> createRoute(@RequestBody RouteVo routeVo, @RequestHeader("userId") Long userId) {
        routeVo.setCreatorId(userId);
        return RUtils.create(
                routeService.createRoute(new RouteDto(routeVo))
        );
    }

    @Operation(summary = "修改路线", description = "修改路线")
    @PostMapping
    public R<Boolean> updateRoute(@RequestBody RouteVo routeVo, @RequestHeader("userId") Long userId, @RequestHeader("Authorities") String authoritiesString) {
        checkRole(routeVo.getCreatorId(), userId, authoritiesString);
        return RUtils.create(
                routeService.updateRoute(new RouteDto(routeVo))
        );
    }

    @Operation(summary = "删除路线", description = "删除路线，请在前端做二次确认")
    @DeleteMapping("/{routeId}")
    public R<Boolean> deleteRoute(@PathVariable("routeId") Long routeId, @RequestHeader("userId") Long userId, @RequestHeader("Authorities") String authoritiesString) {
        RouteDto route = routeService.listRouteById(Collections.singletonList(routeId), HiddenFlagEnum.getAllFlagList()).get(0);
        checkRole(route.getCreatorId(), userId, authoritiesString);
        return RUtils.create(
                routeService.deleteRoute(routeId)
        );
    }

    private void checkRole(Long userId, Long headerUserId, String authoritiesString) {
        List<RoleEnum> userRoleList = JSON.parseArray(authoritiesString).toJavaList(String.class).stream().map(RoleEnum::valueOf).collect(Collectors.toList());
        for (RoleEnum role : userRoleList) {
            if (role.compareTo(RoleEnum.MAP_MANAGER)>=0) return;
        }
        if (!userId.equals(headerUserId)) throw new RuntimeException("无权限进行此路线操作，请检查路线所有者是否为当前用户");
    }


}
