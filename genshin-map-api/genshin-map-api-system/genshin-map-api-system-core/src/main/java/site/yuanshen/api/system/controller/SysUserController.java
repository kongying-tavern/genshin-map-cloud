package site.yuanshen.api.system.controller;

import com.alibaba.fastjson.JSON;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import site.yuanshen.api.system.service.SysUserService;
import site.yuanshen.common.web.response.R;
import site.yuanshen.common.web.response.RUtils;
import site.yuanshen.data.dto.SysUserDto;
import site.yuanshen.data.dto.SysUserPasswordUpdateDto;
import site.yuanshen.data.dto.SysUserUpdateDto;
import site.yuanshen.data.entity.SysUser;
import site.yuanshen.data.enums.RoleEnum;
import site.yuanshen.data.vo.SysUserRegisterVo;
import site.yuanshen.data.vo.SysUserVo;

import java.util.List;
import java.util.stream.Collectors;

/**
 * TODO
 *
 * @author Moment
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/user")
@Transactional
public class SysUserController {

    private final SysUserService userService;

    //todo 放行
    @PostMapping("/register")
    @Transactional
    public R<Long> registerUser(@RequestBody SysUserRegisterVo registerDto) {
        return RUtils.create(userService.register(registerDto));
    }

    @GetMapping("/info/{userId}")
    public R<SysUserVo> getUserInfo(@PathVariable("userId") Long userId,
                                    @RequestHeader("userId") Long headerUserId, @RequestHeader("Authorities") String authoritiesString) {
        List<RoleEnum> userRoleList = JSON.parseArray(authoritiesString).toJavaList(String.class).stream().map(RoleEnum::valueOf).collect(Collectors.toList());
        if (!(userId.equals(headerUserId) || userRoleList.contains(RoleEnum.ADMIN)))
            throw new RuntimeException("权限不足，无法查看其他用户信息");
        return RUtils.create(userService.getUserInfo(userId));
    }

    @PostMapping("/update")
    @Transactional
    public R<Boolean> updateUser(@RequestBody SysUserUpdateDto updateDto,
                                 @RequestHeader("userId") Long headerUserId, @RequestHeader("Authorities") String authoritiesString) {
        List<RoleEnum> userRoleList = JSON.parseArray(authoritiesString).toJavaList(String.class).stream().map(RoleEnum::valueOf).collect(Collectors.toList());
        if (!(updateDto.getUserId().equals(headerUserId) || userRoleList.contains(RoleEnum.ADMIN)))
            throw new RuntimeException("权限不足，无法更改其他用户信息");
        return RUtils.create(userService.updateUser(updateDto));
    }

    @PostMapping("/update_password")
    @Transactional
    public R<Boolean> updateUserPassword(@RequestBody SysUserPasswordUpdateDto passwordUpdateDto,
                                         @RequestHeader("userId") Long headerUserId, @RequestHeader("Authorities") String authoritiesString) {
        List<RoleEnum> userRoleList = JSON.parseArray(authoritiesString).toJavaList(String.class).stream().map(RoleEnum::valueOf).collect(Collectors.toList());
        if (!(passwordUpdateDto.getUserId().equals(headerUserId) || userRoleList.contains(RoleEnum.ADMIN)))
            throw new RuntimeException("权限不足，无法更改其他用户信息");
        return RUtils.create(userService.updatePassword(passwordUpdateDto));
    }

    @DeleteMapping("/{workId}")
    @Transactional
    public R<Boolean> deleteUser(@PathVariable("workId") Long workId) {
        return RUtils.create(userService.deleteUser(workId));
    }
}
