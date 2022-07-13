package site.yuanshen.api.system.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import site.yuanshen.api.system.service.SysRoleService;
import site.yuanshen.api.system.service.SysUserService;
import site.yuanshen.common.core.utils.CachedBeanCopier;
import site.yuanshen.data.dto.SysRoleDto;
import site.yuanshen.data.entity.SysRole;
import site.yuanshen.data.entity.SysUser;
import site.yuanshen.data.entity.SysUserRoleLink;
import site.yuanshen.data.enums.RoleEnum;
import site.yuanshen.data.mapper.SysRoleMapper;
import site.yuanshen.data.mapper.SysUserRoleMapper;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * TODO
 *
 * @author Moment
 */
@Service
@RequiredArgsConstructor
public class SysRoleServiceImpl implements SysRoleService {

    private final SysUserRoleMapper userRoleMapper;
    private final SysRoleMapper roleMapper;
    @Autowired
    private SysUserService userService;

    public Optional<SysRole> getRole(String roleCode) {
        return Optional.ofNullable(roleMapper.selectOne(Wrappers.lambdaQuery(SysRole.class).eq(SysRole::getCode, roleCode)));
    }

    public SysRole getRoleNotNull(String roleCode) {
        return getRole(roleCode).orElseThrow(() -> new RuntimeException("角色不存在"));
    }

    /**
     * @return 团队可用角色列表
     */
    @Override
    public List<SysRoleDto> listRole() {
        return roleMapper.selectList(Wrappers.lambdaQuery(SysRole.class))
                .stream().map((entity) -> CachedBeanCopier.copyProperties(entity, SysRoleDto.class)).collect(Collectors.toList());
    }

    /**
     * 给团队创建新角色
     *
     * @param roleCode 角色代码
     * @return 新角色ID
     */
    @Override
    public Long createRole(String roleCode) {
        SysRole role = RoleEnum.valueOf(roleCode).getRoleBean();
        roleMapper.insert(role);
        return role.getId();
    }

    /**
     * 将角色赋予给用户
     *
     * @param roleCode 角色代码
     * @param id       工作号
     * @return 是否成功
     */
    @Override
    public Boolean addRoleToUser(String roleCode, Long id) {
        SysUser user = userService.getUserNotNull(id);
        SysRole role = getRoleNotNull(roleCode);
        userRoleMapper.insert(new SysUserRoleLink()
                .setUserId(user.getId())
                .setRoleId(role.getId()));
        return true;
    }

    /**
     * 将角色从用户剥夺
     *
     * @param roleCode 角色代码
     * @param id       工作号
     * @return 是否成功
     */
    @Override
    public Boolean removeRoleFromUser(String roleCode, Long id) {
        SysUser user = userService.getUserNotNull(id);
        SysRole role = getRoleNotNull(roleCode);
        SysUserRoleLink userRole = Optional.ofNullable(userRoleMapper.selectOne(Wrappers.lambdaQuery(SysUserRoleLink.class)
                        .eq(SysUserRoleLink::getRoleId, role.getId())
                        .eq(SysUserRoleLink::getUserId, user.getId())))
                .orElseThrow(() -> new RuntimeException("用户并不拥有该权限"));
        userRoleMapper.deleteById(userRole.getId());
        return true;
    }

    /**
     * 删除角色
     *
     * @param roleCode 角色代码
     * @return 是否成功
     */
    @Override
    public Boolean deleteRole(String roleCode) {
        //TODO 增加无法删除角色列表（注意同步修改批量删除函数）
        if (roleCode.equals(RoleEnum.ADMIN.getCode())) throw new RuntimeException("无法删除团队创建者角色");
        SysRole role = roleMapper.selectOne(Wrappers.lambdaQuery(SysRole.class).eq(SysRole::getCode, roleCode));
        if (role == null) throw new RuntimeException("角色不存在");
        roleMapper.deleteById(role.getId());
        return true;
    }

    /**
     * 批量删除角色（未找到的角色通过错误抛出）
     *
     * @param roleCodeList 角色代码列表
     * @return 是否成功
     */
    @Override
    public Boolean deleteRoleBatch(List<String> roleCodeList) {
        //查找角色
        List<SysRole> selectedRole = roleMapper.selectList(Wrappers.lambdaQuery(SysRole.class))
                .stream().filter(role ->
                        roleCodeList.contains(role.getCode()) && !role.getCode().equals(RoleEnum.ADMIN.getCode())
                ).collect(Collectors.toList());
        List<Long> selectedRoleIds = selectedRole.stream().map(SysRole::getId).collect(Collectors.toList());
        //无法查找到的角色，最后通过exception返回
        List<String> wrongRoles = new ArrayList<>(roleCodeList);
        wrongRoles.removeAll(selectedRole.parallelStream().map(SysRole::getCode).collect(Collectors.toList()));
        roleMapper.deleteBatchIds(selectedRoleIds);
        if (wrongRoles.isEmpty()) return true;
        throw new RuntimeException("以下角色未找到：{" + wrongRoles + "}，其余角色已删除");
    }

}
