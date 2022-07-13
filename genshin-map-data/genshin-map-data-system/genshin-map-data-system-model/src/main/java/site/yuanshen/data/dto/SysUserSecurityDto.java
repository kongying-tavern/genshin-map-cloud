package site.yuanshen.data.dto;

import lombok.Data;
import site.yuanshen.data.entity.SysUser;

import java.util.List;

/**
 * 包含密码等安全信息的用户Dto
 *
 * @author Moment
 */
@Data
public class SysUserSecurityDto {

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
    private List<SysRoleDto> roleDtoList;

}
