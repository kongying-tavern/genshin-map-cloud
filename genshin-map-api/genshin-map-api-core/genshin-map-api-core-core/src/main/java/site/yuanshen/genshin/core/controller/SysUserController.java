package site.yuanshen.genshin.core.controller;

import cn.hutool.core.util.ObjUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import site.yuanshen.common.core.exception.GenshinApiException;
import site.yuanshen.common.web.response.R;
import site.yuanshen.common.web.response.RUtils;
import site.yuanshen.common.web.response.WUtils;
import site.yuanshen.data.enums.RoleEnum;
import site.yuanshen.data.vo.*;
import site.yuanshen.data.vo.helper.PageListVo;
import site.yuanshen.genshin.core.service.SysUserService;
import site.yuanshen.genshin.core.utils.UserUtils;
import site.yuanshen.genshin.core.websocket.WebSocketEntrypoint;

import static site.yuanshen.genshin.core.utils.UserUtils.*;

/**
 * 系统用户API
 *
 * @author Moment
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/system/user")
public class SysUserController {

    private final SysUserService userService;
    private final WebSocketEntrypoint webSocket;

    @Operation(summary = "用户注册(管理员权限)", description = "用户注册(管理员权限)，可以注册任意用户名密码的用户")
    @PostMapping("/register")
    public R<Long> registerUser(@RequestBody SysUserRegisterVo registerVo) {
        if (!UserUtils.checkRegisterParamEmpty(registerVo)) throw new GenshinApiException("请检查注册参数，不允许空用户名或者空密码");
        return RUtils.create(userService.register(registerVo));
    }

    @Operation(summary = "qq用户注册", description = "qq用户注册，会对qq的有效性进行验证，并且会关联qq机器人进行验证码验证")
    @PostMapping("/register/qq")
    public R<Long> registerUserByQQ(@RequestBody SysUserRegisterVo registerVo) {
        if (!checkRegisterParamEmpty(registerVo)) throw new GenshinApiException("请检查注册参数，不允许空qq号或者空密码");
        if (!checkRegisterQQParam(registerVo)) throw new GenshinApiException("qq号为空或格式不匹配");
        return RUtils.create(userService.registerByQQ(registerVo));
    }

    @Operation(summary = "用户信息获取", description = "普通用户可以获取到自己的信息，系统管理员可以查看所有用户的")
    @GetMapping("/info/{userId}")
    public R<SysUserVo> getUserInfo(@PathVariable("userId") Long userId,
                                    @RequestHeader("userId") Long headerUserId,
                                    @Parameter(hidden = true)
                                        @RequestHeader("Authorities") String rolesString) {
        if (!userId.equals(headerUserId) && !checkRole(rolesString, RoleEnum.MAP_MANAGER))
            throw new GenshinApiException("权限不足，无法查看其他用户信息");
        return RUtils.create(userService.getUserInfo(userId));
    }


    @Operation(summary = "用户信息批量查询", description = "用户信息批量查询")
    @PostMapping("/info/userList")
    public R<PageListVo<SysUserVo>> getUserList(@RequestBody SysUserSearchVo sysUserSearchVo){
        return RUtils.create(userService.searchPage(sysUserSearchVo));
    }


    @Operation(summary = "用户信息更新", description = "普通用户可以更新自己的信息，系统管理员可以更新所有用户的")
    @PostMapping("/update")
    public R<Boolean> updateUser(@RequestBody SysUserUpdateVo updateVo,
                                 @RequestHeader("userId") Long headerUserId,
                                 @Parameter(hidden = true)
                                     @RequestHeader("Authorities") String rolesString) {
        if (!ObjUtil.equals(headerUserId, updateVo.getUserId()) && !checkRole(rolesString, RoleEnum.ADMIN))
            throw new GenshinApiException("权限不足，无法更新其他用户信息");
        return RUtils.create(userService.updateUser(updateVo));
    }

    @Operation(summary = "用户密码更新", description = "普通用户接口，可以更新自己的密码，需提供旧密码")
    @PostMapping("/update_password")
    public R<Boolean> updateUserPassword(@RequestBody SysUserPasswordUpdateVo updateVo,
                                         @RequestHeader("userId") Long headerUserId) {
        if (!updateVo.getUserId().equals(headerUserId))
            throw new GenshinApiException("无法更改其他用户的密码！");
        //todo ip加入风控链
        if (checkPasswordParamEmpty(updateVo, true)) {
            throw new GenshinApiException("密码不允许为空");
        }
        return RUtils.create(userService.updatePassword(updateVo));
    }


    @Operation(summary = "用户密码修改（管理员接口）", description = "管理员接口，直接修改任意用户密码，无需旧密码")
    @PostMapping("/update_password_by_admin")
    public R<Boolean> updateUserPasswordByAdmin(@RequestBody SysUserPasswordUpdateVo updateVo) {
        if (checkPasswordParamEmpty(updateVo, false)) {
            throw new GenshinApiException("密码不允许为空");
        }
        return RUtils.create(userService.updatePasswordByAdmin(updateVo));
    }

    @Operation(summary = "删除用户", description = "删除用户")
    @DeleteMapping("/{workId}")
    public R<Boolean> deleteUser(@PathVariable("workId") Long workId) {
        return RUtils.create(userService.deleteUser(workId));
    }

    @Operation(summary = "用户踢出", description = "用户踢出")
    @DeleteMapping("/kick_out/{workId}")
    public R<Boolean> kicOutkUser(@PathVariable("workId") Long workId) {
        webSocket.sendToUsers(new String[]{workId.toString()}, WUtils.create("UserKickedOut", null));
        return RUtils.create(true);
    }
}
