package site.yuanshen.api.system.service.impl;

import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import site.yuanshen.api.system.service.SysBasicService;
import site.yuanshen.api.system.service.SysUserService;
import site.yuanshen.common.core.utils.BeanUtils;
import site.yuanshen.data.dto.*;
import site.yuanshen.data.entity.SysRole;
import site.yuanshen.data.entity.SysUser;
import site.yuanshen.data.entity.SysUserRoleLink;
import site.yuanshen.data.enums.RoleEnum;
import site.yuanshen.data.mapper.SysRoleMapper;
import site.yuanshen.data.mapper.SysUserMapper;
import site.yuanshen.data.mapper.SysUserRoleMapper;
import site.yuanshen.data.vo.SysUserRegisterVo;
import site.yuanshen.data.vo.SysUserVo;
import site.yuanshen.data.vo.helper.PageListVo;

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
public class SysUserServiceImpl implements SysUserService {

    private final SysUserMapper userMapper;
    private final SysUserRoleMapper userRoleMapper;
    private final SysRoleMapper sysRoleMapper;
    private final SysBasicService basicService;

    /**
     * @param registerVo 注册封装类
     * @return 用户ID
     */
    @Override
    public Long register(SysUserRegisterVo registerVo) {
        if (basicService.getUser(registerVo.getUsername()).isEmpty()) {
            SysUser user = new SysUser();
            BeanUtils.copyProperties(registerVo, user);
            user.setPassword(PasswordEncoderFactories.createDelegatingPasswordEncoder().encode(user.getPassword()));
            userMapper.insert(user);
            SysRole role = basicService.getRoleNotNull(RoleEnum.MAP_USER.getCode());
            userRoleMapper.insert(new SysUserRoleLink().setRoleId(role.getId()).setUserId(user.getId()));
            return user.getId();
        } else {
            throw new RuntimeException("用户已存在，请检查是否输入正确");
        }
    }

    /**
     * 获取用户信息
     *
     * @param Id 用户ID
     * @return 用户信息
     */
    @Override
    public SysUserVo getUserInfo(Long Id) {
        List<SysUserRoleLink> roleLinks = userRoleMapper.selectList(Wrappers.<SysUserRoleLink>lambdaQuery().eq(SysUserRoleLink::getUserId, Id));
        List<SysRole> roleList = sysRoleMapper.selectList(Wrappers.<SysRole>lambdaQuery().in(SysRole::getId,
                roleLinks.stream().map(SysUserRoleLink::getRoleId).distinct().collect(Collectors.toList())
        ));
        return new SysUserDto(basicService.getUserNotNull(Id))
                .setRoleList(roleList.parallelStream()
                        .map(roleEntity -> (new SysRoleDto(roleEntity)).getVo())
                        .collect(Collectors.toList()))
                .getVo();
    }

    /**
     * @param id 被删除的用户的工作号
     * @return 是否删除成功
     */
    @Override
    public Boolean deleteUser(Long id) {
        SysUser user = basicService.getUserNotNull(id);
        userMapper.deleteById(user.getId());
        return true;
    }

    /**
     * @param updateDto 信息更新封装
     * @return 是否更新成功
     */
    @Override
    public Boolean updateUser(SysUserUpdateDto updateDto) {
        SysUser user = basicService.getUserNotNull(updateDto.getUserId());
        BeanUtils.copyProperties(updateDto, user);
        userMapper.updateById(user);
        return true;
    }

    /**
     * @param passwordUpdateDto 密码更新封装
     * @return 是否更新成功
     */
    @Override
    public Boolean updatePassword(SysUserPasswordUpdateDto passwordUpdateDto) {
        SysUser user = basicService.getUserNotNull(passwordUpdateDto.getUserId());
        PasswordEncoder passwordEncoder = PasswordEncoderFactories.createDelegatingPasswordEncoder();
        if (passwordEncoder.matches(passwordUpdateDto.getOldPassword(), user.getPassword())) {
            user.setPassword(passwordEncoder.encode(passwordUpdateDto.getPassword()));
            userMapper.updateById(user);
            return true;
        }
        throw new RuntimeException("密码错误");
    }


    /**
     * 用户信息批量查询
     * @param sysUserSearchDto
     * @return
     */
    @Override
    public PageListVo<SysUserVo> listPage(SysUserSearchDto sysUserSearchDto) {
        Boolean nickNameSortIsAcs = null;
        Boolean createTimeIsAcs = null;
        List<String> sort = sysUserSearchDto.getSort();
        for (String s :sort){
            if (s.startsWith("createTime")){
                if (s.endsWith("-")){
                    createTimeIsAcs = false;
                }else{
                    createTimeIsAcs = true;
                }
            }

            //Todo gbk 应改为自定义sql
            if (s.startsWith("nickname")){
                if (s.endsWith("-")){
                    nickNameSortIsAcs = false;
                }else{
                    nickNameSortIsAcs = true;
                }
            }
        }

        QueryWrapper<SysUser> wrapper = new QueryWrapper<>();
        wrapper.like(ObjectUtil.isNotNull(sysUserSearchDto.getNickname()), "nickname", sysUserSearchDto.getNickname())
                .like(ObjectUtil.isNotNull(sysUserSearchDto.getUsername()), "username", sysUserSearchDto.getUsername())
                .orderBy(ObjectUtil.isNotNull(createTimeIsAcs), Boolean.TRUE.equals(createTimeIsAcs),"create_time")
                .orderBy(ObjectUtil.isNotNull(nickNameSortIsAcs),Boolean.TRUE.equals(nickNameSortIsAcs),"convert(nickname using gbk) collate gbk_chinese_ci");

        Page<SysUser> sysUserPage = userMapper.selectPage(sysUserSearchDto.getPageEntity(),wrapper);
        return new PageListVo<SysUserVo>()
                .setRecord(sysUserPage.getRecords().stream()
                        .map(SysUserDto::new)
                        .map(SysUserDto::getVo)
                        .collect(Collectors.toList()))
                .setTotal(sysUserPage.getTotal())
                .setSize(sysUserPage.getSize());
    }
}
