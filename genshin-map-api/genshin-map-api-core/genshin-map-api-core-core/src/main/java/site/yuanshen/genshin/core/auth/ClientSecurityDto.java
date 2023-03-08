package site.yuanshen.genshin.core.auth;

import com.alibaba.fastjson2.JSON;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.provider.ClientDetails;
import site.yuanshen.common.core.utils.BeanUtils;
import site.yuanshen.data.dto.SysRoleDto;
import site.yuanshen.data.entity.OauthClientDetails;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 客户端数据封装
 *
 * @author Moment
 */
@Data
@NoArgsConstructor
public class ClientSecurityDto implements ClientDetails {
    
    /**
     * 客户端ID;
     */
    private String clientId;

    /**
     * 客户端密钥;
     */
    private String clientSecret;

    /**
     * 权限范围;
     */
    private String scope;

    /**
     * 鉴权类型;
     */
    private String authorizedGrantTypes;

    /**
     * 重定向地址;
     */
    private String webServerRedirectUri;

    /**
     * 权限;
     */
    private String authorities;

    /**
     * 授权密钥过期时间;
     */
    private Integer accessTokenValidity;

    /**
     * 刷新密钥过期时间;
     */
    private Integer refreshTokenValidity;

    /**
     * 额外信息;
     */
    private String additionalInformation;

    /**
     * 自动同意;
     */
    private Boolean autoApprove;

    public ClientSecurityDto(OauthClientDetails oauthClientDetails) {
        BeanUtils.copyProperties(oauthClientDetails, this);
    }

    @Override
    public String getClientId() {
        return clientId;
    }

    @Override
    public Set<String> getResourceIds() {
        return Collections.emptySet();
    }

    @Override
    public boolean isSecretRequired() {
        return true;
    }

    @Override
    public String getClientSecret() {
        return clientSecret;
    }

    @Override
    public boolean isScoped() {
        return true;
    }

    @Override
    public Set<String> getScope() {
        return Collections.singleton(scope);
    }

    @Override
    public Set<String> getAuthorizedGrantTypes() {
        return new TreeSet<>(List.of(authorizedGrantTypes.split(",")));
    }

    @Override
    public Set<String> getRegisteredRedirectUri() {
        return Collections.singleton(webServerRedirectUri);
    }

    @Override
    public Collection<GrantedAuthority> getAuthorities() {
        return Optional.ofNullable(JSON.parseArray(authorities,String.class)).orElse(new ArrayList<>()).stream().map(SysRoleDto::new).collect(Collectors.toList());
    }

    @Override
    public Integer getAccessTokenValiditySeconds() {
        return accessTokenValidity;
    }

    @Override
    public Integer getRefreshTokenValiditySeconds() {
        return refreshTokenValidity;
    }

    @Override
    public boolean isAutoApprove(String scope) {
        return autoApprove;
    }

    @Override
    public Map<String, Object> getAdditionalInformation() {
        return new TreeMap<>();
    }
}
