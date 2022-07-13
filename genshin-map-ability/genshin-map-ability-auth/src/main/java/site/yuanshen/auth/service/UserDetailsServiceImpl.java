package site.yuanshen.auth.service;


import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import site.yuanshen.auth.model.dto.UserSecurityDto;
import site.yuanshen.common.core.utils.CachedBeanCopier;
import site.yuanshen.data.dto.SysRoleDto;
import site.yuanshen.data.dto.SysUserSecurityDto;
import site.yuanshen.data.entity.SysUser;
import site.yuanshen.data.entity.SysUserRoleLink;
import site.yuanshen.data.mapper.SysRoleMapper;
import site.yuanshen.data.mapper.SysUserMapper;
import site.yuanshen.data.mapper.SysUserRoleMapper;

import java.util.List;
import java.util.stream.Collectors;

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
    private final SysRoleMapper roleMapper;

    @Override
    public UserDetails loadUserByUsername(String userName) throws UsernameNotFoundException {
        return new UserSecurityDto(getUserSecurityByUserName(userName));
    }

    private SysUserSecurityDto getUserSecurityByUserName(String userName) {
        SysUser sysUser = userMapper.selectOne(Wrappers.lambdaQuery(SysUser.class).eq(SysUser::getUsername, userName));
        SysUserSecurityDto sysUserSecurityDto = new SysUserSecurityDto();
        CachedBeanCopier.copyProperties(sysUser, sysUserSecurityDto);
        sysUserSecurityDto.setUserId(sysUser.getId());
        //TODO 代码有待审查
        List<SysRoleDto> sysRoleDtoList = roleMapper.selectBatchIds(userRoleMapper.selectList(Wrappers.lambdaQuery(SysUserRoleLink.class)
                                .eq(SysUserRoleLink::getUserId, sysUser.getId()))
                        .stream()
                        .map(SysUserRoleLink::getRoleId)
                        .collect(Collectors.toList()))
                .stream()
                .map(SysRoleDto::new).collect(Collectors.toList());
        sysUserSecurityDto.setRoleDtoList(sysRoleDtoList);
        return sysUserSecurityDto;
    }

}
