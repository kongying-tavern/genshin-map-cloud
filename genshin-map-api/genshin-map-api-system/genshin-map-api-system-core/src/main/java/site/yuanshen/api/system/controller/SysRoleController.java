package site.yuanshen.api.system.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import site.yuanshen.api.system.service.SysRoleService;
import site.yuanshen.common.web.response.R;
import site.yuanshen.common.web.response.RUtils;
import site.yuanshen.data.dto.SysRoleDto;
import site.yuanshen.data.vo.SysRoleVo;

import java.util.List;

/**
 * TODO
 *
 * @author Moment
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/system/role")
public class SysRoleController {

    private final SysRoleService roleService;

    @GetMapping("/list")
    public R<List<SysRoleDto>> listRole() {
        return RUtils.create(roleService.listRole());
    }

    @PostMapping
    @Transactional
    public R<Boolean> createRole(@RequestBody SysRoleVo roleVo) {
        roleService.createRole(roleVo.getRoleCode());
        return RUtils.create(true);
    }

    @DeleteMapping
    @Transactional
    public R<Boolean> deleteRole(@RequestBody SysRoleVo roleVo) {
        roleService.deleteRole(roleVo.getRoleCode());
        return RUtils.create(true);
    }

    @PostMapping("/user")
    public R<Boolean> addRoleToUser(@RequestBody SysRoleVo roleVo) {
        return RUtils.create(roleService.addRoleToUser(roleVo.getRoleCode(), roleVo.getUserId()));
    }

    @DeleteMapping("/user")
    public R<Boolean> removeRoleFromUser(@RequestBody SysRoleVo roleVo) {
        return RUtils.create(roleService.removeRoleFromUser(roleVo.getRoleCode(), roleVo.getUserId()));
    }

    @DeleteMapping("/batch")
    @Transactional
    public R<Boolean> deleteRoleBatch(@RequestBody List<String> roleCodeList) {
        if (roleCodeList.size() == 0) throw new RuntimeException("权限列表不能为空");
        return RUtils.create(roleService.deleteRoleBatch(roleCodeList));
    }

}
