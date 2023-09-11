package site.yuanshen.genshin.core.auth;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.oauth2.common.DefaultOAuth2AccessToken;
import org.springframework.security.oauth2.config.annotation.configurers.ClientDetailsServiceConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configuration.AuthorizationServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableAuthorizationServer;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerEndpointsConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerSecurityConfigurer;
import org.springframework.security.oauth2.provider.token.TokenEnhancer;
import org.springframework.security.oauth2.provider.token.TokenEnhancerChain;
import org.springframework.security.oauth2.provider.token.TokenStore;
import org.springframework.security.oauth2.provider.token.store.JwtAccessTokenConverter;
import org.springframework.security.oauth2.provider.token.store.JwtTokenStore;
import org.springframework.security.oauth2.provider.token.store.KeyStoreKeyFactory;
import site.yuanshen.data.dto.SysRoleDto;
import site.yuanshen.data.dto.SysUserSecurityDto;


import java.security.KeyPair;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 验证服务器配置
 *
 * @author Moment
 */
@Configuration
@EnableAuthorizationServer
public class AuthorizationServerConfiguration extends AuthorizationServerConfigurerAdapter {
    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private UserDetailsService userDetailsService;

    @Autowired
    private ClientDetailsServiceImpl clientDetailsServiceImpl;

    @Value("${env:prd}")
    private String env;

    @Bean
    public TokenStore tokenStore() {
        return jwtTokenStore();
    }

    @Bean
    public TokenEnhancerChain tokenEnhancerChain() {
        TokenEnhancerChain tokenEnhancerChain = new TokenEnhancerChain();
        List<TokenEnhancer> enhancers = new ArrayList<>();
        enhancers.add((oAuth2AccessToken, oAuth2Authentication) -> {
            //此时principle存在两种情况，一种是客户端登录，只会返回一个String类型的ClientID，另一种则是UserSecurityDto
            Object principal = oAuth2Authentication.getPrincipal();
            Map<String, Object> additionalInfo = new HashMap<>();
            if (principal instanceof SysUserSecurityDto) {
                SysUserSecurityDto userPrincipal = (SysUserSecurityDto) principal;
                List<SysRoleDto> roleList = userPrincipal.getRoleDtoList();
                List<String> roleCodeList = Optional.of(roleList).orElse(new ArrayList<>()).stream().map(SysRoleDto::getCode).collect(Collectors.toList());
                additionalInfo.put("userId", userPrincipal.getUserId());
                additionalInfo.put("userRoles", roleCodeList);
                additionalInfo.put("env", env);
            }
            ((DefaultOAuth2AccessToken) oAuth2AccessToken).setAdditionalInformation(additionalInfo);
            return oAuth2AccessToken;
        });
        enhancers.add(jwtAccessTokenConverter());
        tokenEnhancerChain.setTokenEnhancers(enhancers);
        return tokenEnhancerChain;
    }

    @Bean
    public TokenStore jwtTokenStore() {
        return new JwtTokenStore(jwtAccessTokenConverter());
    }

    @Bean
    public JwtAccessTokenConverter jwtAccessTokenConverter() {
        JwtAccessToken accessTokenConverter = new JwtAccessToken();
        accessTokenConverter.setKeyPair(keyPair());//配置JWT使用的秘钥
        return accessTokenConverter;
    }

    /**
     * 密钥库中获取密钥对(公钥+私钥)
     */
    @Bean
    public KeyPair keyPair() {
        KeyStoreKeyFactory factory = new KeyStoreKeyFactory(new ClassPathResource("oauth2.jks"), "oauth2".toCharArray());
        return factory.getKeyPair("oauth2", "oauth2".toCharArray());
    }

    @Override
    public void configure(ClientDetailsServiceConfigurer clients) throws Exception {
        clients.withClientDetails(clientDetailsServiceImpl);
    }

    @Override
    public void configure(AuthorizationServerEndpointsConfigurer endpoints) {
        endpoints.tokenStore(tokenStore())
                //jwt需要userDetailsService来将用户信息放入jwt中
                .userDetailsService(userDetailsService)
                .authenticationManager(authenticationManager)
                .tokenEnhancer(tokenEnhancerChain())
                .accessTokenConverter(jwtAccessTokenConverter())
                .allowedTokenEndpointRequestMethods(HttpMethod.GET, HttpMethod.POST);
    }

    @Override
    public void configure(AuthorizationServerSecurityConfigurer security) {
        security
                .tokenKeyAccess("permitAll()")
                .checkTokenAccess("permitAll()")
                .allowFormAuthenticationForClients();
    }
}
