package site.yuanshen.genshin.core.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import site.yuanshen.data.entity.SysUser;
import site.yuanshen.data.mapper.SysUserMapper;
import site.yuanshen.genshin.core.service.SysBasicService;

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

}