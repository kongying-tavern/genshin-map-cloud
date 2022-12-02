package site.yuanshen.data.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.List;

/**
 * 用户信息
 *
 * @author Moment
 * @since 2022-04-20 10:18:18
 */
@Data
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Schema(title = "用户信息Vo", description = "用户信息Vo")
public class SysUserVo {

    /**
     * ID
     */
    @Schema(title = "用户ID")
    private Long id;

    /**
     * 用户名
     */
    @Schema(title = "用户名")
    private String username;

    /**
     * 昵称
     */
    @Schema(title = "昵称")
    private String nickname;

    /**
     * QQ
     */
    @Schema(title = "QQ")
    private String qq;

    /**
     * 手机号
     */
    @Schema(title = "手机号")
    private String phone;

    /**
     * 头像链接
     */
    @Schema(title = "头像链接")
    private String logoUrl;

    /**
     * 角色列表
     */
    @Schema(title = "角色列表")
    private List<SysRoleVo> roleList;

}
