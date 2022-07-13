package site.yuanshen.data.mapper;

import site.yuanshen.data.entity.OauthClientDetails;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * Spring默认Oauth2客户端表 Mapper 接口
 *
 * @author Moment
 * @since 2022-07-12 11:05:35
 */
@Mapper
public interface OauthClientDetailsMapper extends BaseMapper<OauthClientDetails> {

}
