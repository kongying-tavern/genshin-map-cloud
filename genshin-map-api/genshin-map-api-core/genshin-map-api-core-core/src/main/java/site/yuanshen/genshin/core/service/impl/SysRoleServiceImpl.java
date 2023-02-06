package site.yuanshen.genshin.core.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import site.yuanshen.data.dto.SysRoleDto;
import site.yuanshen.data.dto.SysRoleLinkDto;
import site.yuanshen.data.entity.SysUser;
import site.yuanshen.data.entity.SysUserRoleLink;
import site.yuanshen.data.enums.RoleEnum;
import site.yuanshen.data.mapper.SysUserRoleMapper;
import site.yuanshen.genshin.core.service.SysBasicService;
import site.yuanshen.genshin.core.service.SysRoleService;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
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
    private final SysBasicService basicService;

    /**
     * @return 团队可用角色列表
     */
    @Override
    public List<SysRoleDto> listRole() {
        List<SysRoleDto> collect = Arrays.stream(RoleEnum.values())
                .sorted(Comparator.comparingInt(RoleEnum::getSort))
                .map(SysRoleDto::new)
                .collect(Collectors.toList());
        return collect;
    }

    /**
     * 创建新角色
     *
     * @param roleDto 角色封装
     * @return 是否成功
     */
    @Override
    @Transactional
    @Deprecated
    public Boolean createRole(SysRoleDto roleDto) {
        //空置
        //SysRole role = roleDto.getEntity();
        //roleMapper.insert(role);
        throw new RuntimeException("该api暂时作废");
    }

    /**
     * 将角色赋予给用户
     *
     * @param roleLinkDto 工作号
     * @return 是否成功
     */
    @Override
    @Transactional
    public Boolean addRoleToUser(SysRoleLinkDto roleLinkDto) {
        SysUser user = basicService.getUserNotNull(roleLinkDto.getUserId());
        RoleEnum role = RoleEnum.getRoleFromId(roleLinkDto.getRoleId());
        //清理旧角色
        userRoleMapper.delete(Wrappers.<SysUserRoleLink>lambdaQuery().eq(SysUserRoleLink::getUserId, roleLinkDto.getUserId()));
        //写入新角色
        userRoleMapper.insert(new SysUserRoleLink()
                .setUserId(user.getId())
                .setRoleId(role.getId()));
        return true;
    }

    /**
     * 将角色从用户剥夺，同时将高于此角色的角色全部剥夺
     *
     * @param roleLinkDto 角色关联数据封装
     * @return 是否成功
     */
    @Override
    @Transactional
    @Deprecated
    public Boolean removeRoleFromUser(SysRoleLinkDto roleLinkDto) {
        throw new RuntimeException("该api暂时作废");
        //SysUser user = basicService.getUserNotNull(roleLinkDto.getUserId());
        //SysRole role = basicService.getRoleNotNullById(roleLinkDto.getRoleId());
        //SysUserRoleLink userRole = Optional.ofNullable(userRoleMapper.selectOne(Wrappers.lambdaQuery(SysUserRoleLink.class)
        //                .lt(SysUserRoleLink::getRoleId, role.getId())
        //                .eq(SysUserRoleLink::getUserId, user.getId())))
        //        .orElseThrow(() -> new RuntimeException("用户并不拥有该权限"));
        //userRoleMapper.deleteById(userRole.getId());
    }

    /**
     * 删除角色
     *
     * @param roleCode 角色代码
     * @return 是否成功
     */
    @Override
    @Transactional
    @Deprecated
    public Boolean deleteRole(String roleCode) {
        throw new RuntimeException("该api暂时作废");
    }

    /**
     * 批量删除角色（未找到的角色通过错误抛出）
     *
     * @param roleCodeList 角色代码列表
     * @return 是否成功
     */
    @Override
    @Transactional
    @Deprecated
    public Boolean deleteRoleBatch(List<String> roleCodeList) {
        throw new RuntimeException("该api暂时作废");
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
