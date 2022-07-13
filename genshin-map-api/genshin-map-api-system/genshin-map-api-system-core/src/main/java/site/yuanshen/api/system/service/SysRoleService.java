package site.yuanshen.api.system.service;

import site.yuanshen.data.dto.SysRoleDto;
import site.yuanshen.data.entity.SysRole;

import java.util.List;
import java.util.Optional;

/**
 *
 */
public interface SysRoleService {


    Optional<SysRole> getRole(String roleCode);

    SysRole getRoleNotNull(String roleCode);

    /**
     * @return 团队可用角色列表
     */
    List<SysRoleDto> listRole();

    /**
     * 给团队创建新角色
     *
     * @param roleCode 角色代码
     * @return 新角色ID
     */
    Long createRole(String roleCode);

    /**
     * 删除角色
     *
     * @param roleCode 角色代码
     * @return 是否成功
     */
    Boolean deleteRole(String roleCode);

    /**
     * 将角色赋予给用户
     *
     * @param roleCode 角色代码
     * @param id       用户ID
     * @return 是否成功
     */
    Boolean addRoleToUser(String roleCode, Long id);

    /**
     * 将角色从用户剥夺
     *
     * @param roleCode 角色代码
     * @param id       用户ID
     * @return 是否成功
     */
    Boolean removeRoleFromUser(String roleCode, Long id);

    /**
     * 批量删除角色（未找到的角色通过错误抛出）
     *
     * @param roleCodeList 角色代码列表
     * @return 是否成功
     */
    Boolean deleteRoleBatch(List<String> roleCodeList);
}
