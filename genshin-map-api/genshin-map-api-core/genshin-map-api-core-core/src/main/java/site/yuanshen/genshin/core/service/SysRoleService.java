package site.yuanshen.genshin.core.service;

import site.yuanshen.data.dto.SysRoleDto;
import site.yuanshen.data.dto.SysRoleLinkDto;
import site.yuanshen.data.entity.SysRole;

import java.util.List;
import java.util.Optional;

/**
 * 系统角色服务实现
 *
 * @author Moment
 */
public interface SysRoleService {

    /**
     * @return 可用角色列表
     */
    List<SysRoleDto> listRole();

    /**
     * 创建新角色
     *
     * @param roleDto 角色封装
     * @return 新角色ID
     */
    Boolean createRole(SysRoleDto roleDto);

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
     * @param roleLinkDto 角色关联数据封装
     * @return 是否成功
     */
    Boolean addRoleToUser(SysRoleLinkDto roleLinkDto);

    /**
     * 将角色从用户剥夺，同时将高于此角色的角色全部剥夺
     *
     * @param roleLinkDto 角色关联数据封装
     * @return 是否成功
     */
    Boolean removeRoleFromUser(SysRoleLinkDto roleLinkDto);

    /**
     * 批量删除角色（未找到的角色通过错误抛出）
     *
     * @param roleCodeList 角色代码列表
     * @return 是否成功
     */
    Boolean deleteRoleBatch(List<String> roleCodeList);
}
