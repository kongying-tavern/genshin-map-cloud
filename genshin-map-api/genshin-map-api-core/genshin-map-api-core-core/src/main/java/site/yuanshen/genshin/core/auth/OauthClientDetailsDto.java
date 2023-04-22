package site.yuanshen.genshin.core.auth;

import cn.hutool.core.convert.Convert;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import com.alibaba.fastjson2.annotation.JSONField;
import org.springframework.security.oauth2.provider.ClientDetails;
import site.yuanshen.common.core.utils.BeanUtils;
import site.yuanshen.data.entity.OauthClientDetails;

import java.util.Collections;
import java.util.Set;
import java.util.TreeSet;


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

    public OauthClientDetailsDto(OauthClientDetails oauthClientDetails) {
        BeanUtils.copy(oauthClientDetails, this);
    }

    @JSONField(serialize = false)
    public OauthClientDetails getEntity() {
        return BeanUtils.copy(this, OauthClientDetails.class);
    }

    /**
     * The resources that this client can access. Can be ignored by callers if empty.
     *
     * @return The resources of this client.
     */
    @Override
    public Set<String> getResourceIds() {
        return new TreeSet<>();
    }

    /**
     * Whether a secret is required to authenticate this client.
     *
     * @return Whether a secret is required to authenticate this client.
     */
    @Override
    public boolean isSecretRequired() {
        return true;
    }

    /**
     * Whether this client is limited to a specific scope. If false, the scope of the authentication request will be
     * ignored.
     *
     * @return Whether this client is limited to a specific scope.
     */
    @Override
    public boolean isScoped() {
        return true;
    }

    /**
     * The pre-defined redirect URI for this client to use during the "authorization_code" access grant. See OAuth spec,
     * section 4.1.1.
     *
     * @return The pre-defined redirect URI for this client.
     */
    @Override
    public Set<String> getRegisteredRedirectUri() {
        return Collections.singleton(webServerRedirectUri);
    }

    /**
     * The access token validity period for this client. Null if not set explicitly (implementations might use that fact
     * to provide a default value for instance).
     *
     * @return the access token validity period
     */
    @Override
    public Integer getAccessTokenValiditySeconds() {
        return accessTokenValidity;
    }

    /**
     * The refresh token validity period for this client. Null for default value set by token service, and
     * zero or negative for non-expiring tokens.
     *
     * @return the refresh token validity period
     */
    @Override
    public Integer getRefreshTokenValiditySeconds() {
        return refreshTokenValidity;
    }

    /**
     * Test whether client needs user approval for a particular scope.
     *
     * @param scope the scope to consider
     * @return true if this client does not need user approval
     */
    @Override
    public boolean isAutoApprove(String scope) {
        return Convert.toBool(autoApprove);
    }
}