package site.yuanshen.auth.service;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.security.oauth2.provider.ClientDetails;
import org.springframework.security.oauth2.provider.ClientDetailsService;
import org.springframework.security.oauth2.provider.ClientRegistrationException;
import org.springframework.stereotype.Service;
import site.yuanshen.auth.model.dto.ClientSecurityDto;
import site.yuanshen.data.entity.OauthClientDetails;
import site.yuanshen.data.mapper.OauthClientDetailsMapper;

/**
 * 客户端详情服务
 *
 * @author Moment
 */
@Primary
@Service
@Slf4j
@RequiredArgsConstructor
public class ClientDetailsServiceImpl implements ClientDetailsService {

    private final OauthClientDetailsMapper clientDetailsMapper;

    /**
     * 通过 Client ID 获取客户端信息
     *
     * @param clientId Client id.
     * @return 获取客户端信息.
     * @throws ClientRegistrationException 客户端不可用
     */
    @Override
    public ClientDetails loadClientByClientId(String clientId) throws ClientRegistrationException {
        OauthClientDetails oauthClientDetails = clientDetailsMapper.selectOne(Wrappers.<OauthClientDetails>lambdaQuery().eq(OauthClientDetails::getClientId, clientId));
        if (oauthClientDetails == null) {
            throw new ClientRegistrationException("客户端不存在");
        }
        return new ClientSecurityDto(oauthClientDetails);
    }
}
