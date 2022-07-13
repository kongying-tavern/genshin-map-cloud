package site.yuanshen.api.system.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import site.yuanshen.api.system.service.SysRoleService;
import site.yuanshen.api.system.service.SysUserService;
import site.yuanshen.common.core.utils.CachedBeanCopier;
import site.yuanshen.data.dto.SysUserDto;
import site.yuanshen.data.dto.SysUserPasswordUpdateDto;
import site.yuanshen.data.dto.SysUserUpdateDto;
import site.yuanshen.data.entity.SysRole;
import site.yuanshen.data.entity.SysUser;
import site.yuanshen.data.entity.SysUserRoleLink;
import site.yuanshen.data.enums.RoleEnum;
import site.yuanshen.data.mapper.SysUserMapper;
import site.yuanshen.data.mapper.SysUserRoleMapper;
import site.yuanshen.data.vo.SysUserRegisterVo;

import java.util.Optional;

/**
 * TODO
 *
 * @author Moment
 */
@Service
@RequiredArgsConstructor
public class SysUserServiceImpl implements SysUserService {

    private final SysUserMapper userMapper;
    private final SysUserRoleMapper userRoleMapper;
    @Autowired
    private SysRoleService roleService;

    public Optional<SysUser> getUser(Long id) {
        return Optional.ofNullable(userMapper.selectOne(Wrappers.lambdaQuery(SysUser.class).eq(SysUser::getId, id)));
    }

    public Optional<SysUser> getUser(String userName) {
        return Optional.ofNullable(userMapper.selectOne(Wrappers.lambdaQuery(SysUser.class).eq(SysUser::getUsername, userName)));
    }

    public SysUser getUserNotNull(Long id) {
        return getUser(id).orElseThrow(() -> new RuntimeException("用户不存在"));
    }

    /**
     * @param registerVo 注册封装类
     * @return 是否注册成功
     */
    @Override
    public Boolean register(SysUserRegisterVo registerVo) {
        if (getUser(registerVo.getUsername()).isEmpty()) {
            SysUser user = new SysUser();
            CachedBeanCopier.copyProperties(registerVo, user);
            user.setPassword(PasswordEncoderFactories.createDelegatingPasswordEncoder().encode(user.getPassword()));
            userMapper.insert(user);
            SysRole role = roleService.getRoleNotNull(RoleEnum.MAP_USER.getCode());
            userRoleMapper.insert(new SysUserRoleLink().setRoleId(role.getId()).setUserId(user.getId()));
            return true;
        } else {
            throw new RuntimeException("用户已存在，请检查是否输入正确");
        }
    }

    /**
     * 获取用户信息
     *
     * @param Id 用户ID
     * @return 用户信息
     */
    @Override
    public SysUserDto getUserInfo(Long Id) {
        return new SysUserDto(getUserNotNull(Id));
    }

    /**
     * @param id 被删除的用户的工作号
     * @return 是否删除成功
     */
    @Override
    public Boolean deleteUser(Long id) {
        SysUser user = getUserNotNull(id);
        userMapper.deleteById(user.getId());
        return true;
    }

    /**
     * @param updateDto 信息更新封装
     * @return 是否更新成功
     */
    @Override
    public Boolean updateUser(SysUserUpdateDto updateDto) {
        SysUser user = getUserNotNull(updateDto.getUserId());
        CachedBeanCopier.copyProperties(updateDto, user);
        userMapper.updateById(user);
        return true;
    }

    /**
     * @param passwordUpdateDto 密码更新封装
     * @return 是否更新成功
     */
    @Override
    public Boolean updatePassword(SysUserPasswordUpdateDto passwordUpdateDto) {
        SysUser user = getUserNotNull(passwordUpdateDto.getUserId());
        PasswordEncoder passwordEncoder = PasswordEncoderFactories.createDelegatingPasswordEncoder();
        if (passwordEncoder.matches(passwordUpdateDto.getOldPassword(), user.getPassword())) {
            user.setPassword(passwordEncoder.encode(passwordUpdateDto.getPassword()));
            userMapper.updateById(user);
            return true;
        }
        throw new RuntimeException("密码错误");
    }
}
