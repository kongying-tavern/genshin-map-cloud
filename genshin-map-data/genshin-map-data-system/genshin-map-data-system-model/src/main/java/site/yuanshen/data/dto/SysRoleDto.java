package site.yuanshen.data.dto;

import lombok.Data;
import site.yuanshen.data.base.CachedBeanCopier;
import site.yuanshen.data.entity.SysRole;
import site.yuanshen.data.vo.SysRoleVo;

/**
 * 角色Dto
 *
 * @author Moment
 */
@Data
public class SysRoleDto {

    /**
     * 角色ID
     */
    private Long id;

    /**
     * 角色名
     */
    private String name;

    /**
     * 角色代码（英文大写）
     */
    private String code;

    /**
     * 角色层级（越大级别越高）
     */
    private Integer sort;

    public SysRoleDto(SysRole sysRole) {
        CachedBeanCopier.copyProperties(sysRole, this);
    }

    public SysRoleDto(SysRoleVo sysRoleVo) {
        CachedBeanCopier.copyProperties(sysRoleVo, this);
    }

    public SysRole getEntity() {
        return CachedBeanCopier.copyProperties(this, SysRole.class);
    }

    public SysRoleVo getVo() {
        return CachedBeanCopier.copyProperties(this, SysRoleVo.class);
    }

}
