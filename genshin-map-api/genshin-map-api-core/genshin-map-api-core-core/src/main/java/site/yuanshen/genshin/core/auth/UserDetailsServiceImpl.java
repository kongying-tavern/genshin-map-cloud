package site.yuanshen.genshin.core.auth;


import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import site.yuanshen.common.core.utils.BeanUtils;
import site.yuanshen.data.dto.SysRoleDto;
import site.yuanshen.data.dto.SysUserSecurityDto;
import site.yuanshen.data.entity.SysUser;
import site.yuanshen.data.entity.SysUserRoleLink;
import site.yuanshen.data.enums.RoleEnum;
import site.yuanshen.data.mapper.SysUserMapper;
import site.yuanshen.data.mapper.SysUserRoleMapper;

import java.util.Collections;
import java.util.Comparator;

/**
 * 用户信息加载服务
 *
 * @author Moment
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final SysUserMapper userMapper;
    private final SysUserRoleMapper userRoleMapper;

    @Override
    public UserDetails loadUserByUsername(String userName) throws UsernameNotFoundException {
        SysUser sysUser = userMapper.selectOne(Wrappers.lambdaQuery(SysUser.class).eq(SysUser::getUsername, userName));
        SysUserSecurityDto sysUserSecurityDto = new SysUserSecurityDto();
        BeanUtils.copyProperties(sysUser, sysUserSecurityDto);
        sysUserSecurityDto.setUserId(sysUser.getId());
        SysRoleDto roleDto = userRoleMapper.selectList(Wrappers.<SysUserRoleLink>lambdaQuery()
                        .eq(SysUserRoleLink::getUserId, sysUser.getId()))
                .stream()
                .map(SysUserRoleLink::getRoleId)
                .map(RoleEnum::getRoleFromId)
                .min(Comparator.comparingInt(RoleEnum::getSort))
                .map(SysRoleDto::new)
                .orElseThrow(() -> new RuntimeException("用户未绑定角色"));
        sysUserSecurityDto.setRoleDtoList(Collections.singletonList(roleDto));
        return sysUserSecurityDto;
    }

}
