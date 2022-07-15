package site.yuanshen.data.dto;

import lombok.Data;
import site.yuanshen.common.core.utils.BeanUtils;
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
        BeanUtils.copyProperties(userRoleLink, this);
    }

    public SysRoleLinkDto(SysRoleLinkVo linkVo) {
        BeanUtils.copyProperties(linkVo, this);
    }

}
