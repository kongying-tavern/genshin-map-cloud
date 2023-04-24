package site.yuanshen.genshin.core.service.impl;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import site.yuanshen.common.core.utils.BeanUtils;
import site.yuanshen.data.dto.*;
import site.yuanshen.data.entity.SysUser;
import site.yuanshen.data.enums.RoleEnum;
import site.yuanshen.data.mapper.SysUserMapper;
import site.yuanshen.data.vo.SysUserRegisterVo;
import site.yuanshen.data.vo.SysUserVo;
import site.yuanshen.data.vo.helper.PageListVo;
import site.yuanshen.genshin.core.service.SysBasicService;
import site.yuanshen.genshin.core.service.SysUserService;

import java.util.*;
import java.util.stream.Collectors;

/**
 * TODO
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
        ResponseEntity<String> response = gbkRestTemplate.getForEntity("http://r.qzone.qq.com/fcg-bin/cgi_get_portrait.fcg?uins="+qq, String.class);
        String qqInfo = null;
            qqInfo = response.getBody();
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
        userMapper.deleteById(user.getId());
        return true;
    }

    /**
     * @param updateDto 信息更新封装
     * @return 是否更新成功
     */
    @Override
    @Transactional
    public Boolean updateUser(SysUserUpdateDto updateDto) {
        SysUser user = basicService.getUserNotNull(updateDto.getUserId());
        BeanUtils.copy(updateDto, user);
        userMapper.updateById(user);
        return true;
    }

    /**
     * @param passwordUpdateDto 密码更新封装
     * @return 是否更新成功
     */
    @Override
    @Transactional
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
        wrapper.like(StrUtil.isNotBlank(sysUserSearchDto.getNickname()), "nickname", sysUserSearchDto.getNickname())
                .like(StrUtil.isNotBlank(sysUserSearchDto.getUsername()), "username", sysUserSearchDto.getUsername())
                .orderBy(ObjectUtil.isNotNull(createTimeIsAcs), Boolean.TRUE.equals(createTimeIsAcs), "create_time")
                .orderBy(ObjectUtil.isNotNull(nickNameSortIsAcs), Boolean.TRUE.equals(nickNameSortIsAcs), "convert(nickname using gbk) collate gbk_chinese_ci");

        //此处mbp的分页优化有问题，关闭分页优化，减少报错日志
        Page<SysUser> sysUserPage = userMapper.selectPage(sysUserSearchDto.getPageEntity().setOptimizeCountSql(false), wrapper);

        return new PageListVo<SysUserVo>()
                .setRecord(sysUserPage.getRecords().parallelStream()
                        .map(SysUserDto::new)
                        .map(SysUserDto::getVo)
                        .collect(Collectors.toList()))
                .setTotal(sysUserPage.getTotal())
                .setSize(sysUserPage.getSize());
    }
}
