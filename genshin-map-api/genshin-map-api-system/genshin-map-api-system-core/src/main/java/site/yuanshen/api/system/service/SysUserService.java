package site.yuanshen.api.system.service;

import site.yuanshen.data.dto.SysUserDto;
import site.yuanshen.data.dto.SysUserPasswordUpdateDto;
import site.yuanshen.data.dto.SysUserUpdateDto;
import site.yuanshen.data.vo.SysUserRegisterVo;
import site.yuanshen.data.vo.SysUserVo;

/**
 * 用户相关服务类接口
 *
 * @author Moment
 */
public interface SysUserService {

    /**
     * @param registerDto 注册封装类
     * @return 用户ID
     */
    Long register(SysUserRegisterVo registerDto);

    /**
     * 获取用户信息
     *
     * @param Id 用户ID
     * @return 用户信息
     */
    SysUserVo getUserInfo(Long Id);

    /**
     * @param id 被删除的用户的id
     * @return 是否删除成功
     */
    Boolean deleteUser(Long id);

    /**
     * @param updateDto 信息更新封装
     * @return 是否更新成功
     */
    Boolean updateUser(SysUserUpdateDto updateDto);

    /**
     * @param passwordUpdateDto 密码更新封装
     * @return 是否更新成功
     */
    Boolean updatePassword(SysUserPasswordUpdateDto passwordUpdateDto);

}
