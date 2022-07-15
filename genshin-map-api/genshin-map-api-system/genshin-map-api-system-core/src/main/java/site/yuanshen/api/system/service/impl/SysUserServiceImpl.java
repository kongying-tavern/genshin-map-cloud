package site.yuanshen.api.system.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import site.yuanshen.api.system.service.SysBasicService;
import site.yuanshen.api.system.service.SysUserService;
import site.yuanshen.common.core.utils.BeanUtils;
import site.yuanshen.data.dto.SysRoleDto;
import site.yuanshen.data.dto.SysUserDto;
import site.yuanshen.data.dto.SysUserPasswordUpdateDto;
import site.yuanshen.data.dto.SysUserUpdateDto;
import site.yuanshen.data.entity.SysRole;
import site.yuanshen.data.entity.SysUser;
import site.yuanshen.data.entity.SysUserRoleLink;
import site.yuanshen.data.enums.RoleEnum;
import site.yuanshen.data.mapper.SysRoleMapper;
import site.yuanshen.data.mapper.SysUserMapper;
import site.yuanshen.data.mapper.SysUserRoleMapper;
import site.yuanshen.data.vo.SysUserRegisterVo;
import site.yuanshen.data.vo.SysUserVo;

import java.util.List;
import java.util.stream.Collectors;

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
    private final SysRoleMapper sysRoleMapper;
    private final SysBasicService basicService;

    /**
     * @param registerVo 注册封装类
     * @return 用户ID
     */
    @Override
    public Long register(SysUserRegisterVo registerVo) {
        if (basicService.getUser(registerVo.getUsername()).isEmpty()) {
            SysUser user = new SysUser();
            BeanUtils.copyProperties(registerVo, user);
            user.setPassword(PasswordEncoderFactories.createDelegatingPasswordEncoder().encode(user.getPassword()));
            userMapper.insert(user);
            SysRole role = basicService.getRoleNotNull(RoleEnum.MAP_USER.getCode());
            userRoleMapper.insert(new SysUserRoleLink().setRoleId(role.getId()).setUserId(user.getId()));
            return user.getId();
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
    public SysUserVo getUserInfo(Long Id) {
        List<SysUserRoleLink> roleLinks = userRoleMapper.selectList(Wrappers.<SysUserRoleLink>lambdaQuery().eq(SysUserRoleLink::getUserId, Id));
        List<SysRole> roleList = sysRoleMapper.selectList(Wrappers.<SysRole>lambdaQuery().in(SysRole::getId,
                roleLinks.stream().map(SysUserRoleLink::getRoleId).distinct().collect(Collectors.toList())
        ));
        return new SysUserDto(basicService.getUserNotNull(Id))
                .setRoleList(roleList.parallelStream()
                        .map(roleEntity -> (new SysRoleDto(roleEntity)).getVo())
                        .collect(Collectors.toList()))
                .getVo();
    }

    /**
     * @param id 被删除的用户的工作号
     * @return 是否删除成功
     */
    @Override
    public Boolean deleteUser(Long id) {
        SysUser user = basicService.getUserNotNull(id);
        userMapper.deleteById(user.getId());
        return true;
    }

    /**
     * @param updateDto 信息更新封装
     * @return 是否更新成功
     */
    @Override
    public Boolean updateUser(SysUserUpdateDto updateDto) {
        SysUser user = basicService.getUserNotNull(updateDto.getUserId());
        BeanUtils.copyProperties(updateDto, user);
        userMapper.updateById(user);
        return true;
    }

    /**
     * @param passwordUpdateDto 密码更新封装
     * @return 是否更新成功
     */
    @Override
    public Boolean updatePassword(SysUserPasswordUpdateDto passwordUpdateDto) {
        SysUser user = basicService.getUserNotNull(passwordUpdateDto.getUserId());
        PasswordEncoder passwordEncoder = PasswordEncoderFactories.createDelegatingPasswordEncoder();
        if (passwordEncoder.matches(passwordUpdateDto.getOldPassword(), user.getPassword())) {
            user.setPassword(passwordEncoder.encode(passwordUpdateDto.getPassword()));
            userMapper.updateById(user);
            return true;
        }
        throw new RuntimeException("密码错误");
    }
}
