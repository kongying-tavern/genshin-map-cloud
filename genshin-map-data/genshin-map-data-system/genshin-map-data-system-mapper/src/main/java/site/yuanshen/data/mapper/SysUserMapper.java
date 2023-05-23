package site.yuanshen.data.mapper;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Constants;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import site.yuanshen.data.entity.SysUser;

import java.util.List;

/**
 * 系统用户表 Mapper 接口
 *
 * @since 2023-04-22 12:16:38
 */
@Mapper
public interface SysUserMapper extends BaseMapper<SysUser> {

    IPage<SysUser> searchUserPage(IPage<SysUser> page, @Param(Constants.WRAPPER) Wrapper<SysUser> queryWrapper, @Param("nickNameSortIsAcs") Boolean nickNameSortIsAcs);

    List<SysUser> selectUserWithDelete(@Param("userIdList") List<Long> userIdList);

}
