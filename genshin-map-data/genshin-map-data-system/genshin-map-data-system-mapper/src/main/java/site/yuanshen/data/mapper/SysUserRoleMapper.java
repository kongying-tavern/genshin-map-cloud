package site.yuanshen.data.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import site.yuanshen.data.entity.SysUserRoleLink;

/**
 * 用户-角色联系表 Mapper 接口
 *
 * @author Moment
 * @since 2022-04-09 01:32:10
 */
@Mapper
public interface SysUserRoleMapper extends BaseMapper<SysUserRoleLink> {

}
