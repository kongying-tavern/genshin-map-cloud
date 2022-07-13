package site.yuanshen.api.system.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import site.yuanshen.api.system.service.SysBasicService;
import site.yuanshen.data.entity.SysRole;
import site.yuanshen.data.entity.SysUser;
import site.yuanshen.data.mapper.SysRoleMapper;
import site.yuanshen.data.mapper.SysUserMapper;

import java.util.Optional;

/**
 * TODO
 *
 * @author Moment
 */
@Service
@RequiredArgsConstructor
public class SysBasicServiceImpl implements SysBasicService {

    private final SysUserMapper userMapper;

    private final SysRoleMapper roleMapper;

    /**
     * 此方法建议只用于同级service
     *
     * @param Id 用户ID
     * @return 用户实体类Optional
     */
    @Override
    public Optional<SysUser> getUser(Long id) {
        return Optional.ofNullable(userMapper.selectOne(Wrappers.lambdaQuery(SysUser.class).eq(SysUser::getId, id)));
    }

    /**
     * 此方法建议只用于同级service
     *
     * @param id 用户ID
     * @return 用户实体类
     */
    @Override
    public SysUser getUserNotNull(Long id) {
        return getUser(id).orElseThrow(() -> new RuntimeException("用户不存在"));
    }

    /**
     * 此方法建议只用于同级service
     *
     * @param userName 用户名
     * @return 用户实体类Optional
     */
    @Override
    public Optional<SysUser> getUser(String userName) {
        return Optional.ofNullable(userMapper.selectOne(Wrappers.lambdaQuery(SysUser.class).eq(SysUser::getUsername, userName)));
    }

    /**
     * 此方法建议只用于同级service
     *
     * @param roleCode 角色代码
     * @return 角色实体类Optional
     */
    @Override
    public Optional<SysRole> getRole(String roleCode) {
        return Optional.ofNullable(roleMapper.selectOne(Wrappers.lambdaQuery(SysRole.class).eq(SysRole::getCode, roleCode)));
    }

    /**
     * 此方法建议只用于同级service
     *
     * @param roleCode 角色代码
     * @return 角色实体类
     */
    @Override
    public SysRole getRoleNotNull(String roleCode) {
        return getRole(roleCode).orElseThrow(() -> new RuntimeException("角色不存在"));
    }

    /**
     * 此方法建议只用于同级service
     *
     * @param roleId 角色ID
     * @return 角色实体类Optional
     */
    @Override
    public Optional<SysRole> getRoleById(Long roleId) {
        return Optional.ofNullable(roleMapper.selectOne(Wrappers.lambdaQuery(SysRole.class).eq(SysRole::getId, roleId)));
    }

    /**
     * 此方法建议只用于同级service
     *
     * @param roleId 角色ID
     * @return 角色实体类
     */
    @Override
    public SysRole getRoleNotNullById(Long roleId) {
        return getRoleById(roleId).orElseThrow(() -> new RuntimeException("角色不存在"));
    }
}
