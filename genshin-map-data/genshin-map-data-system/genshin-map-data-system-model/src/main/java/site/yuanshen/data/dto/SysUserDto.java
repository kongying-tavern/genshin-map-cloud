package site.yuanshen.data.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.Accessors;
import site.yuanshen.common.core.utils.CachedBeanCopier;;
import site.yuanshen.data.entity.SysUser;
import site.yuanshen.data.vo.SysRoleVo;
import site.yuanshen.data.vo.SysUserVo;

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
@Accessors(chain = true)
public class SysUserDto {

    /**
     * ID
     */
    private Long id;

    /**
     * 用户名
     */
    private String username;

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
     * 角色列表
     */
    private List<SysRoleVo> roleList;

    public SysUserDto(SysUser user) {
        CachedBeanCopier.copyProperties(user, this);
    }

    public SysUserVo getVo() {
        return CachedBeanCopier.copyProperties(this, SysUserVo.class);
    }

}
