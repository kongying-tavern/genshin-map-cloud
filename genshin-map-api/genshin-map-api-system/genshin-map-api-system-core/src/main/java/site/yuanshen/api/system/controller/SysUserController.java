package site.yuanshen.api.system.controller;

import com.alibaba.fastjson.JSON;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import site.yuanshen.api.system.service.SysUserService;
import site.yuanshen.common.web.response.R;
import site.yuanshen.common.web.response.RUtils;
import site.yuanshen.data.dto.SysUserPasswordUpdateDto;
import site.yuanshen.data.dto.SysUserSearchDto;
import site.yuanshen.data.dto.SysUserUpdateDto;
import site.yuanshen.data.enums.RoleEnum;
import site.yuanshen.data.vo.SysUserRegisterVo;
import site.yuanshen.data.vo.SysUserSearchVo;
import site.yuanshen.data.vo.SysUserVo;
import site.yuanshen.data.vo.helper.PageListVo;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 系统用户API
 *
 * @author Moment
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/user")
public class SysUserController {

    private final SysUserService userService;

    @PostMapping("/register")
    public R<Long> registerUser(@RequestBody SysUserRegisterVo registerDto) {
        if (checkRegisterEmpty(registerDto)) throw new RuntimeException("请检查注册参数，不允许空用户名或者空密码");
        return RUtils.create(userService.register(registerDto));
    }

    @PostMapping("/register/qq")
    public R<Long> registerUserByQQ(@RequestBody SysUserRegisterVo registerDto) {
        if (checkRegisterEmpty(registerDto)) throw new RuntimeException("请检查注册参数，不允许空qq号或者空密码");
        return RUtils.create(userService.registerByQQ(registerDto));
    }

    private boolean checkRegisterEmpty(SysUserRegisterVo registerDto) {
        return registerDto.getUsername() == null || "".equals(registerDto.getUsername()) || registerDto.getPassword() == null || "".equals(registerDto.getPassword());
    }

    @GetMapping("/info/{userId}")
    public R<SysUserVo> getUserInfo(@PathVariable("userId") Long userId,
                                    @RequestHeader("userId") Long headerUserId, @RequestHeader("Authorities") String authoritiesString) {
        List<RoleEnum> userRoleList = JSON.parseArray(authoritiesString).toJavaList(String.class).stream().map(RoleEnum::valueOf).collect(Collectors.toList());
        if (!(userId.equals(headerUserId) || userRoleList.contains(RoleEnum.ADMIN)))
            throw new RuntimeException("权限不足，无法查看其他用户信息");
        return RUtils.create(userService.getUserInfo(userId));
    }


    @Operation(summary = "用户信息批量查询", description = "用户信息批量查询")
    @PostMapping("/info/userList")
    public R<PageListVo<SysUserVo>> getUserList(@RequestBody SysUserSearchVo sysUserSearchVo){
        return RUtils.create(userService.listPage(new SysUserSearchDto(sysUserSearchVo)));
    }


    @PostMapping("/update")
    public R<Boolean> updateUser(@RequestBody SysUserUpdateDto updateDto,
                                 @RequestHeader("userId") Long headerUserId, @RequestHeader("Authorities") String authoritiesString) {
        List<RoleEnum> userRoleList = JSON.parseArray(authoritiesString).toJavaList(String.class).stream().map(RoleEnum::valueOf).collect(Collectors.toList());
        if (!(updateDto.getUserId().equals(headerUserId) || userRoleList.contains(RoleEnum.ADMIN)))
            throw new RuntimeException("权限不足，无法更改其他用户信息");
        return RUtils.create(userService.updateUser(updateDto));
    }

    @PostMapping("/update_password")
    public R<Boolean> updateUserPassword(@RequestBody SysUserPasswordUpdateDto updateDto,
                                         @RequestHeader("userId") Long headerUserId, @RequestHeader("Authorities") String authoritiesString) {
        List<RoleEnum> userRoleList = JSON.parseArray(authoritiesString).toJavaList(String.class).stream().map(RoleEnum::valueOf).collect(Collectors.toList());
        if (!(updateDto.getUserId().equals(headerUserId) || userRoleList.contains(RoleEnum.ADMIN)))
            throw new RuntimeException("权限不足，无法更改其他用户信息");
        if (updateDto.getPassword() == null || "".equals(updateDto.getPassword()))
            throw new RuntimeException("密码不允许为空");
        return RUtils.create(userService.updatePassword(updateDto));
    }

    @DeleteMapping("/{workId}")
    public R<Boolean> deleteUser(@PathVariable("workId") Long workId) {
        return RUtils.create(userService.deleteUser(workId));
    }
}
