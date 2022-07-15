package site.yuanshen.auth.model.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import site.yuanshen.common.core.utils.BeanUtils;
import site.yuanshen.data.dto.SysUserSecurityDto;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 包含密码等安全信息的用户Dto
 *
 * @author Moment
 */
@Data
@NoArgsConstructor
public class UserSecurityDto implements UserDetails {

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 用户名
     */
    private String username;

    /**
     * 密码
     */
    private String password;

    /**
     * 昵称
     */
    private String nickname;

    /**
     * QQ
     */
    private String qq;

    /**
     * 手机号
     */
    private String phone;

    /**
     * 角色
     */
    private List<RoleSecurityDto> roleDtoList;

    public UserSecurityDto(SysUserSecurityDto sysUserSecurityDto) {
        BeanUtils.copyProperties(sysUserSecurityDto, this);
        this.roleDtoList = sysUserSecurityDto.getRoleDtoList()
                .stream().map(
                        roleDto -> BeanUtils.copyProperties(roleDto, RoleSecurityDto.class)
                ).collect(Collectors.toList());
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return roleDtoList;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}
