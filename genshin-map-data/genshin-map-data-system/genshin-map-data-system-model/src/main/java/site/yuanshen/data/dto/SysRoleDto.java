package site.yuanshen.data.dto;

import lombok.Data;
import site.yuanshen.common.core.utils.BeanUtils;
import site.yuanshen.data.entity.SysRole;
import site.yuanshen.data.enums.RoleEnum;
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
     * 角色层级（越小级别越高）
     */
    private Integer sort;

    public SysRoleDto(SysRole sysRole) {
        BeanUtils.copyProperties(sysRole, this);
    }

    public SysRoleDto(RoleEnum roleEnum) {
        this.id = roleEnum.getId();
        this.name = roleEnum.getName();
        this.code = roleEnum.getCode();
        this.sort = roleEnum.getSort();
    }

    public SysRoleDto(SysRoleVo sysRoleVo) {
        BeanUtils.copyProperties(sysRoleVo, this);
    }

    public SysRole getEntity() {
        return BeanUtils.copyProperties(this, SysRole.class);
    }

    public SysRoleVo getVo() {
        return BeanUtils.copyProperties(this, SysRoleVo.class);
    }

}
