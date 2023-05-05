package site.yuanshen.genshin.core.utils;

import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson2.JSON;
import site.yuanshen.data.enums.RoleEnum;
import site.yuanshen.data.vo.SysUserPasswordUpdateVo;
import site.yuanshen.data.vo.SysUserRegisterVo;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 用户相关业务工具类
 *
 * @author Moment
 */
@SuppressWarnings("BooleanMethodIsAlwaysInverted")
public class UserUtils {

    /**
     * 校验注册参数是否为空
     *
     * @param registerVo 注册视图层封装
     * @return 参数是否为空
     */
    public static boolean checkRegisterParamEmpty(SysUserRegisterVo registerVo) {
        return StrUtil.isNotBlank(registerVo.getUsername()) && StrUtil.isNotBlank(registerVo.getPassword());
    }

    /**
     * @param registerVo 注册视图层封装
     * @return qq号是否符合基本规则
     */
    public static boolean checkRegisterQQParam(SysUserRegisterVo registerVo) {
        String qq = registerVo.getUsername();
        return StrUtil.isNotEmpty(qq) && qq.matches("[1-9]\\d{4,10}");
    }

    /**
     * 校验密码修改参数是否为空
     *
     * @param updateVo 更新密码视图层封装
     * @param checkOldPassword 是否校验旧密码
     * @return 参数是否为空
     */
    @SuppressWarnings("RedundantIfStatement")
    public static boolean checkPasswordParamEmpty(SysUserPasswordUpdateVo updateVo, boolean checkOldPassword) {
        if (StrUtil.isEmpty(updateVo.getPassword())) return false;
        if (checkOldPassword && StrUtil.isEmpty(updateVo.getOldPassword())) return false;
        return true;
    }

    /**
     * 校验角色是否符合角色要求
     *
     * @param rolesString 角色代码List字符串
     * @param role        需比对的角色
     * @return 角色是否包含地图管理员权限
     */
    public static boolean checkRole(String rolesString, RoleEnum role) {
        List<RoleEnum> userRoleList = JSON.parseArray(rolesString, String.class).stream().map(RoleEnum::valueOf).collect(Collectors.toList());
        return checkRole(userRoleList, role);
    }

    /**
     * 校验角色是否符合角色要求
     *
     * @param roleEnumsList 角色枚举List
     * @param role          需比对的角色
     * @return 角色是否包含地图管理员权限
     */
    public static boolean checkRole(List<RoleEnum> roleEnumsList, RoleEnum role) {
        if (roleEnumsList.size() != 1 && !roleEnumsList.contains(RoleEnum.ADMIN))
            throw new RuntimeException("目标权限异常，请联系管理员处理");
        for (RoleEnum roleEnum : roleEnumsList) {
            if (roleEnum.getSort() <= role.getSort()) return true;
        }
        return false;
    }

}
