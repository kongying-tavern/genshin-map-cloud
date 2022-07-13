package site.yuanshen.data.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import site.yuanshen.data.base.CachedBeanCopier;
import site.yuanshen.data.entity.SysUserRoleLink;

/**
 * 角色关联Dto
 *
 * @author Moment
 */
@Data
public class SysRoleLinkVo {

    /**
     * 用户ID
     */
    @Schema(title = "用户ID")
    private Long userId;

    /**
     * 角色ID
     */
    @Schema(title = "角色ID")
    private Long roleId;

}
