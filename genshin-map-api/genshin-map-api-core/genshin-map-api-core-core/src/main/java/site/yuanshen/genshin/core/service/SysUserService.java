package site.yuanshen.genshin.core.service;

import cn.hutool.core.collection.CollUtil;
import com.alibaba.fastjson2.JSON;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import site.yuanshen.common.core.exception.GenshinApiException;
import site.yuanshen.common.core.utils.BeanUtils;
import site.yuanshen.data.dto.SysUserDto;
import site.yuanshen.data.dto.SysUserSearchDto;
import site.yuanshen.data.enums.RoleEnum;
import site.yuanshen.data.vo.*;
import site.yuanshen.data.vo.helper.PageListVo;
import site.yuanshen.genshin.core.dao.SysUserDao;

import java.util.List;

/**
 * 用户相关服务类接口实现
 *
 * @author Moment
 */
@Service
public class SysUserService {

    private final SysUserDao userDao;

    private final RestTemplate gbkRestTemplate;

    public SysUserService(SysUserDao basicService,
                          @Qualifier("gbkRestTemplate") RestTemplate gbkRestTemplate) {
        this.userDao = basicService;
        this.gbkRestTemplate = gbkRestTemplate;
    }

    /**
     * @param registerVo 注册封装类
     * @return 用户ID
     */
    @Transactional
    public Long register(SysUserRegisterVo registerVo) {
        String username = registerVo.getUsername();
        String password = registerVo.getPassword();
        if (userDao.getUser(username).isPresent()) throw new GenshinApiException("用户已存在，请检查是否输入正确");
        SysUserDto userDto = new SysUserDto(registerVo)
                .withPassword(PasswordEncoderFactories.createDelegatingPasswordEncoder().encode(password))
                .withRoleId(RoleEnum.MAP_USER.ordinal());
        return userDao.insertUser(userDto);
    }

    /**
     * @param registerVo 注册封装类（此处用户名为QQ）
     * @return 用户ID
     */
    @Transactional
    public Long registerByQQ(SysUserRegisterVo registerVo) {
        String qq = registerVo.getUsername();
        String password = registerVo.getPassword();
        if (userDao.getUser(qq).isPresent()) throw new GenshinApiException("qq号已被注册，请联系管理员");

        ResponseEntity<String> response = gbkRestTemplate.getForEntity("https://r.qzone.qq.com/fcg-bin/cgi_get_portrait.fcg?uins=" + qq, String.class);
        String qqInfo = response.getBody();
        if (!response.getStatusCode().equals(HttpStatus.OK) || qqInfo == null || !qqInfo.contains("portraitCallBack")) {
            throw new GenshinApiException("服务器无法连接qq服务器，请先检查使用的qq号是否正确");
        }
        String qqName;
        try {
            qqName = JSON
                    .parseObject(qqInfo.substring(17, qqInfo.length() - 1))
                    .getJSONArray(qq)
                    .getString(6);
        } catch (Exception e) {
            throw new GenshinApiException("QQ号有误，请使用真实的QQ号进行注册");
        }
        String qqLogo = "https://q1.qlogo.cn/g?b=qq&nk=" + qq + "&s=640";

        return userDao.insertUser(
                new SysUserDto()
                        .withQq(qq)
                        .withUsername(qq)
                        .withPassword(PasswordEncoderFactories.createDelegatingPasswordEncoder().encode(password))
                        .withRoleId(RoleEnum.MAP_USER.ordinal())
                        .withNickname(qqName)
                        .withLogo(qqLogo)
        );
    }

    /**
     * 获取用户信息
     *
     * @param Id 用户ID
     * @return 用户信息
     */
    public SysUserVo getUserInfo(Long Id) {
        SysUserDto userDto = userDao.getUserNotNull(Id);
        return userDto.getVo();
    }

    /**
     * @param id 被删除的用户的工作号
     * @return 是否删除成功
     */
    @Transactional
    public Boolean deleteUser(Long id) {
        SysUserDto userDto = userDao.getUserNotNull(id);
        return userDao.deleteUser(userDto);
    }

    /**
     * @param updateVo 信息更新封装
     * @return 是否更新成功
     */
    @Transactional
    public Boolean updateUser(SysUserUpdateVo updateVo) {
        SysUserDto userDto = userDao.getUserNotNull(updateVo.getUserId());
        BeanUtils.copyNotNull(updateVo, userDto);
        return userDao.updateUser(userDto);
    }

    /**
     * @param updateVo 密码更新封装
     * @return 是否更新成功
     */
    @Transactional
    public Boolean updatePassword(SysUserPasswordUpdateVo updateVo) {
        Long userId = updateVo.getUserId();
        String password = updateVo.getPassword();
        String oldPassword = updateVo.getOldPassword();

        SysUserDto userDto = userDao.getUserNotNull(userId);
        PasswordEncoder passwordEncoder = PasswordEncoderFactories.createDelegatingPasswordEncoder();
        if (!passwordEncoder.matches(oldPassword, userDto.getPassword())) {
            throw new GenshinApiException("密码错误");
        }
        userDto.setPassword(passwordEncoder.encode(password));
        return userDao.updateUser(userDto);
    }

    /**
     * @param updateVo 密码更新封装
     * @return 是否更新成功
     */
    public Boolean updatePasswordByAdmin(SysUserPasswordUpdateVo updateVo) {
        Long userId = updateVo.getUserId();
        String password = updateVo.getPassword();

        SysUserDto userDto = userDao.getUserNotNull(userId);

        PasswordEncoder passwordEncoder = PasswordEncoderFactories.createDelegatingPasswordEncoder();
        userDto.setPassword(passwordEncoder.encode(password));

        return userDao.updateUser(userDto);
    }

    /**
     * 用户信息批量查询
     *
     * @param searchVo 用户列表查询前端封装
     * @return 用户信息封装List
     */
    public PageListVo<SysUserVo> searchPage(SysUserSearchVo searchVo) {
        SysUserSearchDto searchDto = new SysUserSearchDto(searchVo);

        List<String> sort = CollUtil.emptyIfNull(searchDto.getSort());
        boolean nickNameSortIsAcs = false;
        boolean createTimeIsAcs = false;
        for (String s : sort) {
            if (s.startsWith("createTime")) {
                createTimeIsAcs = !s.endsWith("-");
            }
            if (s.startsWith("nickname")) {
                nickNameSortIsAcs = !s.endsWith("-");
            }
        }

        return userDao.searchPage(searchDto, createTimeIsAcs, nickNameSortIsAcs);
    }
}
