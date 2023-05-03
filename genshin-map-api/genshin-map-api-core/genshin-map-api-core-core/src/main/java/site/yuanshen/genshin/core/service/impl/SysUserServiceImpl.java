package site.yuanshen.genshin.core.service.impl;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import site.yuanshen.common.core.utils.BeanUtils;
import site.yuanshen.data.base.BaseEntity;
import site.yuanshen.data.dto.SysUserDto;
import site.yuanshen.data.dto.SysUserSearchDto;
import site.yuanshen.data.entity.SysUser;
import site.yuanshen.data.enums.RoleEnum;
import site.yuanshen.data.mapper.SysUserMapper;
import site.yuanshen.data.vo.*;
import site.yuanshen.data.vo.helper.PageListVo;
import site.yuanshen.genshin.core.service.SysBasicService;
import site.yuanshen.genshin.core.service.SysUserService;

import java.util.List;
import java.util.stream.Collectors;

import static cn.hutool.core.util.ObjectUtil.isNotNull;
import static cn.hutool.core.util.StrUtil.isNotBlank;

/**
 * 用户相关服务类接口实现
 *
 * @author Moment
 */
@Service
public class SysUserServiceImpl implements SysUserService {

    private final SysUserMapper userMapper;
    private final SysBasicService basicService;

    private final RestTemplate gbkRestTemplate;

    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    public SysUserServiceImpl(SysUserMapper userMapper, SysBasicService basicService,
                              @Qualifier("gbkRestTemplate") RestTemplate gbkRestTemplate) {
        this.userMapper = userMapper;
        this.basicService = basicService;
        this.gbkRestTemplate = gbkRestTemplate;
    }

    /**
     * @param registerVo 注册封装类
     * @return 用户ID
     */
    @Override
    @Transactional
    public Long register(SysUserRegisterVo registerVo) {
        if (basicService.getUser(registerVo.getUsername()).isPresent()) throw new RuntimeException("用户已存在，请检查是否输入正确");
        SysUser user = new SysUser();
        userMapper.insert(BeanUtils.copy(registerVo, user)
                .withPassword(PasswordEncoderFactories.createDelegatingPasswordEncoder().encode(user.getPassword()))
                .withRoleId(RoleEnum.MAP_USER.ordinal()));
        return user.getId();
    }

    /**
     * @param registerDto 注册封装类（此处用户名为QQ）
     * @return 用户ID
     */
    @Override
    @Transactional
    public Long registerByQQ(SysUserRegisterVo registerDto) {
        String qq = registerDto.getUsername();
        if (qq.isBlank() || !qq.matches("[1-9]\\d{4,10}")) {
            throw new RuntimeException("qq号为空或格式不匹配");
        }
        if (basicService.getUser(qq).isPresent()) {
            throw new RuntimeException("qq号已被注册，请联系管理员");
        }
        ResponseEntity<String> response = gbkRestTemplate.getForEntity("https://r.qzone.qq.com/fcg-bin/cgi_get_portrait.fcg?uins="+qq, String.class);
        String qqInfo = response.getBody();
        if (!response.getStatusCode().equals(HttpStatus.OK) || qqInfo == null || !qqInfo.contains("portraitCallBack")) {
            throw new RuntimeException("服务器无法连接qq服务器，获取头像失败");
        }
        String qqName;
        try {
            qqInfo = qqInfo.substring(17, qqInfo.length() - 1);
            JSONObject qqInfoJson = JSON.parseObject(qqInfo);
            JSONArray infoArray = qqInfoJson.getJSONArray(qq);
            qqName = infoArray.getString(6);
        } catch (Exception e) {
            throw new RuntimeException("QQ号有误，请使用真实的QQ号进行注册");
        }
        String qqLogo = "https://q1.qlogo.cn/g?b=qq&nk="+qq+"&s=640";
        SysUser user = new SysUser()
                .withQq(qq)
                .withUsername(qq)
                .withPassword(PasswordEncoderFactories.createDelegatingPasswordEncoder().encode(registerDto.getPassword()))
                .withRoleId(RoleEnum.MAP_USER.ordinal())
                .withNickname(qqName)
                .withLogo(qqLogo);
        userMapper.insert(user);
        return user.getId();
    }

    /**
     * 获取用户信息
     *
     * @param Id 用户ID
     * @return 用户信息
     */
    @Override
    public SysUserVo getUserInfo(Long Id) {
        SysUser user = basicService.getUserNotNull(Id);
        return new SysUserDto(user)
                .getVo();
    }

    /**
     * @param id 被删除的用户的工作号
     * @return 是否删除成功
     */
    @Override
    @Transactional
    public Boolean deleteUser(Long id) {
        SysUser user = basicService.getUserNotNull(id);
        return userMapper.deleteById(user.getId()) == 1;
    }

    /**
     * @param updateDto 信息更新封装
     * @return 是否更新成功
     */
    @Override
    @Transactional
    public Boolean updateUser(SysUserUpdateVo updateDto) {
        SysUser user = basicService.getUserNotNull(updateDto.getUserId());
        BeanUtils.copy(updateDto, user);
        return userMapper.updateById(user) == 1;
    }

    /**
     * @param updateVo 密码更新封装
     * @return 是否更新成功
     */
    @Override
    @Transactional
    public Boolean updatePassword(SysUserPasswordUpdateVo updateVo) {
        Long userId = updateVo.getUserId();
        String password = updateVo.getPassword();
        String oldPassword = updateVo.getOldPassword();

        SysUser user = basicService.getUserNotNull(userId);
        PasswordEncoder passwordEncoder = PasswordEncoderFactories.createDelegatingPasswordEncoder();
        if (passwordEncoder.matches(oldPassword, user.getPassword())) {
            user.setPassword(passwordEncoder.encode(password));
            return userMapper.updateById(user) == 1;
        }
        throw new RuntimeException("密码错误");
    }

    /**
     * @param updateVo 密码更新封装
     * @return 是否更新成功
     */
    @Override
    public Boolean updatePasswordByAdmin(SysUserPasswordUpdateVo updateVo) {
        Long userId = updateVo.getUserId();
        String password = updateVo.getPassword();

        SysUser user = basicService.getUserNotNull(userId);

        PasswordEncoder passwordEncoder = PasswordEncoderFactories.createDelegatingPasswordEncoder();
        user.setPassword(passwordEncoder.encode(password));

        return userMapper.updateById(user) == 1;
    }

    /**
     * 用户信息批量查询
     *
     * @param searchVo 用户列表查询前端封装
     * @return 用户信息封装List
     */
    @Override
    public PageListVo<SysUserVo> listPage(SysUserSearchVo searchVo) {
        SysUserSearchDto searchDto = new SysUserSearchDto(searchVo);
        Boolean nickNameSortIsAcs = null;
        Boolean createTimeIsAcs = null;
        List<String> sort = searchDto.getSort();
        for (String s :sort){
            if (s.startsWith("createTime")){
                createTimeIsAcs = !s.endsWith("-");
            }
            if (s.startsWith("nickname")){
                nickNameSortIsAcs = !s.endsWith("-");
            }
        }

        LambdaQueryWrapper<SysUser> wrapper = Wrappers.<SysUser>lambdaQuery()
                .like(isNotBlank(searchDto.getNickname()), SysUser::getNickname, searchDto.getNickname())
                .like(isNotBlank(searchDto.getUsername()), SysUser::getUsername, searchDto.getUsername())
                .orderBy(isNotNull(createTimeIsAcs), Boolean.TRUE.equals(createTimeIsAcs), BaseEntity::getCreateTime);

        //此处mbp的分页优化有问题，关闭分页优化，减少报错日志
        IPage<SysUser> sysUserPage = userMapper.searchUserPage(searchDto.getPageEntity().setOptimizeCountSql(false), wrapper, nickNameSortIsAcs);

        return new PageListVo<SysUserVo>()
                .setRecord(sysUserPage.getRecords().stream()
                        .map(SysUserDto::new)
                        .map(SysUserDto::getVo)
                        .collect(Collectors.toList()))
                .setTotal(sysUserPage.getTotal())
                .setSize(sysUserPage.getSize());
    }
}
