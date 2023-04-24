package site.yuanshen.genshin.core.auth;


import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import site.yuanshen.common.core.utils.BeanUtils;
import site.yuanshen.data.entity.SysUser;
import site.yuanshen.data.enums.RoleEnum;
import site.yuanshen.data.mapper.SysUserMapper;

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

    @Override
    public UserDetails loadUserByUsername(String userName) throws UsernameNotFoundException {
        SysUser sysUser = userMapper.selectOne(Wrappers.lambdaQuery(SysUser.class).eq(SysUser::getUsername, userName));
        SysUserSecurityDto sysUserSecurityDto = new SysUserSecurityDto();
        BeanUtils.copy(sysUser, sysUserSecurityDto);
        sysUserSecurityDto.setUserId(sysUser.getId());
        Integer roleId = sysUser.getRoleId();
        if (roleId == null) {
            throw new InsufficientAuthenticationException("用户未获得角色授权");
        }
        sysUserSecurityDto.setRoleEnumList(Collections.singletonList(RoleEnum.getRoleFromId(roleId)));
        return sysUserSecurityDto;
    }

}
