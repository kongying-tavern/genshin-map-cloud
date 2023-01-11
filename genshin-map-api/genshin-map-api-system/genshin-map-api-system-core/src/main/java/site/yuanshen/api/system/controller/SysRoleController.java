package site.yuanshen.api.system.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import site.yuanshen.api.system.service.SysRoleService;
import site.yuanshen.common.web.response.R;
import site.yuanshen.common.web.response.RUtils;
import site.yuanshen.data.dto.SysRoleDto;
import site.yuanshen.data.dto.SysRoleLinkDto;
import site.yuanshen.data.vo.SysRoleLinkVo;
import site.yuanshen.data.vo.SysRoleVo;

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
@RequestMapping("/role")
@Tag(name = "role", description = "角色管理API")
public class SysRoleController {

    private final SysRoleService roleService;

    @Operation(summary = "返回可用角色列表", description = "返回可用角色列表")
    @GetMapping("/list")
    public R<List<SysRoleVo>> listRole() {
        return RUtils.create(roleService.listRole().stream().map(SysRoleDto::getVo).collect(Collectors.toList()));
    }

    @Operation(summary = "创建新的角色", description = "创建新的角色")
    @PostMapping
    public R<Boolean> createRole(@RequestBody SysRoleVo sysRoleVo) {
        //接口已空置，弃用中
        roleService.createRole(new SysRoleDto(sysRoleVo));
        return RUtils.create(true);
    }

    @Operation(summary = "将角色赋予给用户", description = "将角色赋予给用户")
    @PutMapping("/user")
    public R<Boolean> addRoleToUser(@RequestBody SysRoleLinkVo roleLinkVo) {
        return RUtils.create(
                roleService.addRoleToUser(new SysRoleLinkDto(roleLinkVo))
        );
    }

    @Operation(summary = "将角色从用户剥夺", description = "将角色从用户剥夺，同时将高于此角色的角色全部剥夺")
    @DeleteMapping("/user")
    public R<Boolean> removeRoleFromUser(@RequestBody SysRoleLinkVo roleLinkVo) {
        return RUtils.create(
                roleService.removeRoleFromUser(new SysRoleLinkDto(roleLinkVo))
        );
    }

    @Operation(summary = "删除角色", description = "删除角色")
    @DeleteMapping
    public R<Boolean> deleteRole(@RequestBody String code) {
        //接口已空置，弃用中
        roleService.deleteRole(code);
        return RUtils.create(true);
    }

    @Operation(summary = "批量删除角色", description = "批量删除角色（未找到的角色通过错误抛出）")
    @DeleteMapping("/batch")
    public R<Boolean> deleteRoleBatch(@RequestBody List<String> roleCodeList) {
        //接口已空置，弃用中
        if (roleCodeList.size() == 0) throw new RuntimeException("权限列表不能为空");
        return RUtils.create(roleService.deleteRoleBatch(roleCodeList));
    }

}
