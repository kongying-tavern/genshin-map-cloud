package site.yuanshen.genshin.core.auth;

import lombok.Data;
import org.springframework.security.core.userdetails.UserDetails;
import site.yuanshen.data.enums.RoleEnum;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 包含密码等安全信息的用户Dto
 *
 * @author Moment
 */
@Data
public class SysUserSecurityDto  implements UserDetails {

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
    private List<RoleEnum> roleEnumList;

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public List<RoleAuthDto> getAuthorities() {
        return roleEnumList.stream().map(RoleAuthDto::new).collect(Collectors.toList());
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
