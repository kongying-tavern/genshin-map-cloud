package site.yuanshen.data.mapper;

import site.yuanshen.data.entity.SysUser;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 系统用户表 Mapper 接口
 *
 * @since 2023-04-22 12:16:38
 */
@Mapper
public interface SysUserMapper extends BaseMapper<SysUser> {

}
