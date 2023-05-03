package site.yuanshen.data.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.With;

/**
 * 用户信息更新前端封装
 *
 * @author Moment
 */
@Data
@With
@NoArgsConstructor
@AllArgsConstructor
@Schema(title = "用户信息更新前端封装", description = "用户信息更新前端封装, 此封装不包括密码")
public class SysUserUpdateVo {

    /**
     * 用户ID
     */
    private Long userId;

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
     * 头像链接
     */
    private String logoUrl;

    /**
     * 角色ID
     *
     * @see site.yuanshen.data.enums.RoleEnum
     */
    private Integer roleId;

}
