package site.yuanshen.genshin.core.auth;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.annotation.JSONField;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.With;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.provider.ClientDetails;
import site.yuanshen.common.core.utils.BeanUtils;
import site.yuanshen.data.entity.OauthClientDetails;
import site.yuanshen.data.enums.RoleEnum;

import java.util.*;
import java.util.stream.Collectors;


/**
 * Spring默认Oauth2客户端表路数据封装
 *
 * @since 2023-04-22 04:19:03
 */
@Data
@With
@NoArgsConstructor
@AllArgsConstructor
@Schema(title = "OauthClientDetails数据封装", description = "Spring默认Oauth2客户端表数据封装")
public class OauthClientDetailsDto implements ClientDetails {

    /**
     * 乐观锁
     */
    private Long version;

    /**
     * ID
     */
    private Long id;

    /**
     * 客户端ID
     */
    private String clientId;

    /**
     * 客户端密钥
     */
    private String clientSecret;

    /**
     * 权限范围
     */
    private String scope;

    /**
     * 鉴权类型
     */
    private String authorizedGrantTypes;

    /**
     * 重定向地址
     */
    private String webServerRedirectUri;

    /**
     * 权限
     */
    private String authorities;

    /**
     * 授权密钥过期时间
     */
    private Integer accessTokenValidity;

    /**
     * 刷新密钥过期时间
     */
    private Integer refreshTokenValidity;

    /**
     * 额外信息
     */
    private String additionalInformation;

    /**
     * 自动同意
     */
    private String autoApprove;

    public OauthClientDetailsDto(OauthClientDetails entity) {
        this.version = entity.getVersion();
        this.id = entity.getId();
        this.clientId = entity.getClientId();
        this.clientSecret = entity.getClientSecret();
        this.scope = entity.getScope();
        this.authorizedGrantTypes = entity.getAuthorizedGrantTypes();
        this.webServerRedirectUri = entity.getWebServerRedirectUri();
        this.authorities = entity.getAuthorities();
        this.accessTokenValidity = entity.getAccessTokenValidity();
        this.refreshTokenValidity = entity.getRefreshTokenValidity();
        this.additionalInformation = entity.getAdditionalInformation();
        this.autoApprove = entity.getAutoApprove();
    }

    @JSONField(serialize = false)
    public OauthClientDetails getEntity() {
        return BeanUtils.copy(this, OauthClientDetails.class);
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
        return Optional.ofNullable(JSON.parseArray(authorities,String.class)).orElse(new ArrayList<>()).stream().map(RoleEnum::getRoleFromCode).map(RoleAuthDto::new).collect(Collectors.toList());
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
        return Boolean.parseBoolean(autoApprove);
    }

    @Override
    public Map<String, Object> getAdditionalInformation() {
        return new TreeMap<>();
    }
}