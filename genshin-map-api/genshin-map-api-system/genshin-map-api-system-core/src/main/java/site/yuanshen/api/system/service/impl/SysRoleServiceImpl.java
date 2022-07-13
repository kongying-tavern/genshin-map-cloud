package site.yuanshen.api.system.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import site.yuanshen.api.system.service.SysBasicService;
import site.yuanshen.api.system.service.SysRoleService;
import site.yuanshen.api.system.service.SysUserService;
import site.yuanshen.data.dto.SysRoleDto;
import site.yuanshen.data.dto.SysRoleLinkDto;
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
    private final SysBasicService basicService;

    /**
     * @return 团队可用角色列表
     */
    @Override
    public List<SysRoleDto> listRole() {
        List<SysRoleDto> collect = roleMapper.selectList(Wrappers.query())
                .stream()
                .map(SysRoleDto::new).collect(Collectors.toList());
        return collect;
    }

    /**
     * 创建新角色
     *
     * @param roleDto 角色封装
     * @return 是否成功
     */
    @Override
    public Boolean createRole(SysRoleDto roleDto) {
        //空置
        //SysRole role = roleDto.getEntity();
        //roleMapper.insert(role);
        return true;
    }

    /**
     * 将角色赋予给用户
     *
     * @param roleLinkDto 工作号
     * @return 是否成功
     */
    @Override
    public Boolean addRoleToUser(SysRoleLinkDto roleLinkDto) {
        SysUser user = basicService.getUserNotNull(roleLinkDto.getUserId());
        SysRole role = basicService.getRoleNotNullById(roleLinkDto.getRoleId());
        userRoleMapper.insert(new SysUserRoleLink()
                .setUserId(user.getId())
                .setRoleId(role.getId()));
        return true;
    }

    /**
     * 将角色从用户剥夺
     *
     * @param roleLinkDto 角色关联数据封装
     * @return 是否成功
     */
    @Override
    public Boolean removeRoleFromUser(SysRoleLinkDto roleLinkDto) {
        SysUser user = basicService.getUserNotNull(roleLinkDto.getUserId());
        SysRole role = basicService.getRoleNotNullById(roleLinkDto.getRoleId());
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
        //空置
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
        //空置函数
        return true;
        ////查找角色
        //List<SysRole> selectedRole = roleMapper.selectList(Wrappers.lambdaQuery(SysRole.class))
        //        .stream().filter(role ->
        //                //筛选存在的角色，并排除管理员角色
        //                roleCodeList.contains(role.getCode()) && !role.getCode().equals(RoleEnum.ADMIN.getCode())
        //        ).collect(Collectors.toList());
        //List<Long> selectedRoleIds = selectedRole.stream().map(SysRole::getId).collect(Collectors.toList());
        ////无法查找到的角色，最后通过exception返回
        //List<String> wrongRoles = new ArrayList<>(roleCodeList);
        //wrongRoles.removeAll(selectedRole.parallelStream().map(SysRole::getCode).collect(Collectors.toList()));
        //roleMapper.deleteBatchIds(selectedRoleIds);
        //if (wrongRoles.isEmpty()) return true;
        //throw new RuntimeException("以下角色未找到：{" + wrongRoles + "}，其余角色已删除");
    }

}
