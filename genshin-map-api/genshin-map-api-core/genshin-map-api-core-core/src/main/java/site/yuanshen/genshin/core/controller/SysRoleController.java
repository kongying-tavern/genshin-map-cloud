package site.yuanshen.genshin.core.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import site.yuanshen.common.core.utils.BeanUtils;
import site.yuanshen.common.web.response.R;
import site.yuanshen.common.web.response.RUtils;
import site.yuanshen.data.enums.RoleEnum;
import site.yuanshen.data.vo.SysRoleVo;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 角色管理API
 *
 * @author Moment
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/system/role")
@Tag(name = "role", description = "角色管理API")
public class SysRoleController {

    @Operation(summary = "返回可用角色列表", description = "返回可用角色列表")
    @GetMapping("/list")
    public R<List<SysRoleVo>> listRole() {
        return RUtils.create(Arrays.stream(RoleEnum.values())
                .sorted(Comparator.comparingInt(RoleEnum::getSort))
                .map(e -> BeanUtils.copy(e, SysRoleVo.class).withId(e.ordinal()))
                .collect(Collectors.toList()));
    }

}
