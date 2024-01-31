package site.yuanshen.genshin.core.dao;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import site.yuanshen.common.core.utils.PgsqlUtils;
import site.yuanshen.data.dto.SysUserDto;
import site.yuanshen.data.dto.SysUserSearchDto;
import site.yuanshen.data.entity.SysUser;
import site.yuanshen.data.mapper.SysUserMapper;
import site.yuanshen.data.vo.SysUserVo;
import site.yuanshen.data.vo.helper.PageListVo;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static cn.hutool.core.text.CharSequenceUtil.isNotBlank;

/**
 * 部分内部用的公共用户、角色服务（规避循环依赖）
 *
 * @author Moment
 */
@Service
@RequiredArgsConstructor
public class SysUserDao {

    private final SysUserMapper userMapper;


    /**
     * 此方法建议只用于同级service
     *
     * @param id 用户ID
     * @return 用户实体类Optional
     */
    public Optional<SysUser> getUser(Long id) {
        return Optional.ofNullable(userMapper.selectOne(Wrappers.lambdaQuery(SysUser.class).eq(SysUser::getId, id)));
    }

    /**
     * 此方法建议只用于同级service
     *
     * @param id 用户ID
     * @return 用户实体类
     */
    public SysUserDto getUserNotNull(Long id) {
        return new SysUserDto(getUser(id).orElseThrow(() -> new RuntimeException("用户不存在")));
    }

    /**
     * 此方法建议只用于同级service
     *
     * @param userName 用户名
     * @return 用户实体类Optional
     */
    public Optional<SysUser> getUser(String userName) {
        return Optional.ofNullable(userMapper.selectOne(Wrappers.lambdaQuery(SysUser.class).eq(SysUser::getUsername, userName)));
    }

    public Long insertUser(SysUserDto userDto) {
        SysUser user = userDto.getEntity();
        int result = userMapper.insert(user);
        return user.getId();
    }

    public boolean updateUser(SysUserDto userDto) {
        int result = userMapper.updateById(userDto.getEntity());
        return result == 1;
    }

    public boolean deleteUser(SysUserDto userDto) {
        int result = userMapper.deleteById(userDto.getId());
        return result == 1;
    }

    public PageListVo<SysUserVo> searchPage(SysUserSearchDto searchDto, boolean nickNameSortIsAcs) {
        QueryWrapper<SysUser> wrapper = Wrappers.<SysUser>query();
        final List<PgsqlUtils.Sort<SysUser>> sortList = PgsqlUtils.toSort(searchDto.getSort(), SysUser.class, Set.of("id", "nickname", "createTime", "updateTime"));
        wrapper = PgsqlUtils.sortWrapper(wrapper, sortList);

        LambdaQueryWrapper<SysUser> queryWrapper = wrapper.lambda()
                .like(isNotBlank(searchDto.getNickname()), SysUser::getNickname, searchDto.getNickname())
                .like(isNotBlank(searchDto.getUsername()), SysUser::getUsername, searchDto.getUsername())
                .in(CollUtil.isNotEmpty(searchDto.getRoleIds()), SysUser::getRoleId, searchDto.getRoleIds());

        //此处mbp的分页优化有问题，关闭分页优化，减少报错日志
        IPage<SysUser> sysUserPage = userMapper.searchUserPage(searchDto.getPageEntity().setOptimizeCountSql(false), queryWrapper, nickNameSortIsAcs);

        return new PageListVo<SysUserVo>()
                .setRecord(sysUserPage.getRecords().stream()
                        .map(SysUserDto::new)
                        .map(SysUserDto::getVo)
                        .collect(Collectors.toList()))
                .setTotal(sysUserPage.getTotal())
                .setSize(sysUserPage.getSize());
    }

}
