package site.yuanshen.api.system.service;

import site.yuanshen.data.dto.SysUserDto;
import site.yuanshen.data.dto.SysUserPasswordUpdateDto;
import site.yuanshen.data.dto.SysUserUpdateDto;
import site.yuanshen.data.entity.SysUser;
import site.yuanshen.data.vo.SysUserRegisterVo;

import java.util.Optional;

/**
 * 用户相关服务类接口
 *
 * @author Moment
 */
public interface SysUserService {

    /**
     * 此方法建议只用于同级service
     *
     * @param Id 用户ID
     * @return 用户实体类Optional
     */
    Optional<SysUser> getUser(Long Id);

    /**
     * 此方法建议只用于同级service
     *
     * @param id 用户ID
     * @return 用户实体类
     */
    SysUser getUserNotNull(Long id);

    /**
     * @param registerDto 注册封装类
     * @return 是否注册成功
     */
    Boolean register(SysUserRegisterVo registerDto);

    /**
     * 获取用户信息
     *
     * @param Id 用户ID
     * @return 用户信息
     */
    SysUserDto getUserInfo(Long Id);

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
