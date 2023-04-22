package site.yuanshen.data.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import site.yuanshen.data.entity.OauthClientDetails;

/**
 * Spring默认Oauth2客户端表 Mapper 接口
 *
 * @since 2023-04-22 12:16:38
 */
@Mapper
public interface OauthClientDetailsMapper extends BaseMapper<OauthClientDetails> {

}
