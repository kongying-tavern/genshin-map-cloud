package site.yuanshen.genshin.core.auth;

import org.springframework.security.oauth2.common.DefaultOAuth2AccessToken;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.token.store.JwtAccessTokenConverter;
import org.springframework.stereotype.Component;

import java.util.Date;

@Component
public class JwtAccessToken extends JwtAccessTokenConverter {
    @Override
    public OAuth2AccessToken enhance(OAuth2AccessToken accessToken, OAuth2Authentication authentication) {
        DefaultOAuth2AccessToken defaultOAuth2AccessToken = new DefaultOAuth2AccessToken(accessToken);
        defaultOAuth2AccessToken.setExpiration(new Date(System.currentTimeMillis() + 1000 * 1800 ));
        return super.enhance(defaultOAuth2AccessToken, authentication);
    }
}
