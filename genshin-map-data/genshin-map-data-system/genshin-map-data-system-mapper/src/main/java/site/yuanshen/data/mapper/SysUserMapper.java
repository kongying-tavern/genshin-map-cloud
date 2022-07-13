package site.yuanshen.data.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import site.yuanshen.data.entity.SysUser;

/**
 * 用户系统表 Mapper 接口
 *
 * @author Moment
 * @since 2022-04-09 01:32:10
 */
@Mapper
public interface SysUserMapper extends BaseMapper<SysUser> {

}
