package site.yuanshen.data.dto;

import lombok.Data;
import site.yuanshen.data.base.CachedBeanCopier;
import site.yuanshen.data.entity.SysUserRoleLink;
import site.yuanshen.data.vo.SysRoleLinkVo;

/**
 * 角色关联Dto
 *
 * @author Moment
 */
@Data
public class SysRoleLinkDto {

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 角色ID
     */
    private Long roleId;

    public SysRoleLinkDto(SysUserRoleLink userRoleLink) {
        CachedBeanCopier.copyProperties(userRoleLink, this);
    }

    public SysRoleLinkDto(SysRoleLinkVo linkVo) {
        CachedBeanCopier.copyProperties(linkVo, this);
    }

}
