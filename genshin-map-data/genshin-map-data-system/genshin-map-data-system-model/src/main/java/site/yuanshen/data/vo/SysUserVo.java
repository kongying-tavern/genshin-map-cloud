package site.yuanshen.data.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.Accessors;
import site.yuanshen.common.core.utils.CachedBeanCopier;;
import site.yuanshen.data.entity.SysUser;

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
     * 角色列表
     */
    @Schema(title = "角色列表")
    private List<SysRoleVo> roleList;

}
