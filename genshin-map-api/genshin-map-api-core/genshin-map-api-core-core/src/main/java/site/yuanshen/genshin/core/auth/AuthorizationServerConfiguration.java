package site.yuanshen.genshin.core.auth;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
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
import site.yuanshen.data.dto.adapter.user.access.AccessPathVo;
import site.yuanshen.data.enums.RoleEnum;
import site.yuanshen.genshin.core.service.SysActionLogService;
import site.yuanshen.genshin.core.service.SysUserDeviceService;
import site.yuanshen.genshin.core.utils.ClientUtils;

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

    @Autowired
    private SysActionLogService sysActionLogService;

    @Autowired
    private SysUserDeviceService sysUserDeviceService;

    @Value("${env:prd}")
    private String env;

    /**
     * JWT 密钥库位置，支持 file: 与 classpath: 前缀。
     * 不提供内置默认值：密钥库不得随制品分发。
     */
    @Value("${oauth2.jks-location:}")
    private String jksLocation;

    /**
     * JWT 密钥库口令
     */
    @Value("${oauth2.jks-password:}")
    private String jksPassword;

    /**
     * JWT 密钥别名
     */
    @Value("${oauth2.jks-alias:oauth2}")
    private String jksAlias;

    /**
     * JWT 密钥口令，缺省时复用密钥库口令
     */
    @Value("${oauth2.jks-key-password:}")
    private String jksKeyPassword;

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
                Long userId = userPrincipal.getUserId();
                List<AccessPathVo> accessPaths = new ArrayList<>();
                ClientUtils.ClientInfo clientInfo = ClientUtils.getClientInfo(null, null);

                boolean isDeviceAccessible = sysUserDeviceService.checkDeviceAccess(
                    userPrincipal.getUserId(), userPrincipal.getAccessPolicyList(), accessPaths, clientInfo
                );
                Map<String, Object> logExtraData = new HashMap<>();
                logExtraData.put("accessPaths", accessPaths);

                sysActionLogService.addNewLog(userId, "LOGIN", !isDeviceAccessible, logExtraData, clientInfo);
                List<RoleEnum> roleList = userPrincipal.getRoleEnumList();
                List<String> roleCodeList = Optional.of(roleList).orElse(new ArrayList<>()).stream()
                    .map(RoleEnum::getCode).collect(Collectors.toList());
                additionalInfo.put("userId", userId);
                additionalInfo.put("userRoles", roleCodeList);
                additionalInfo.put("env", env);
                additionalInfo.put("message", isDeviceAccessible ? "" : "账户登录设备或IP存在波动，请注意账号安全");
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
     * <p>
     * 密钥库通过 oauth2.jks-location 从外部挂载读取（file:/… 或 classpath:/…），
     * 口令通过 oauth2.jks-password 注入，均为必填项：仓库与制品中不再包含任何密钥库。
     * 缺少配置时在启动阶段直接失败，避免带病启动后签发无效 token。
     */
    @Bean
    public KeyPair keyPair() {
        if (isBlank(jksLocation) || isBlank(jksPassword)) {
            throw new IllegalStateException(
                "OAuth2 JWT 密钥库未配置：请设置 oauth2.jks-location 与 oauth2.jks-password"
                    + "（或环境变量 OAUTH2_JKS_LOCATION / OAUTH2_JKS_PASSWORD）；"
                    + "密钥库不再内置在制品中，需外部挂载后启动"
            );
        }
        final Resource keyStoreResource = resolveKeyStoreResource(jksLocation);
        if (!keyStoreResource.exists()) {
            throw new IllegalStateException("OAuth2 JWT 密钥库不存在: " + jksLocation);
        }
        final String keyPassword = isBlank(jksKeyPassword) ? jksPassword : jksKeyPassword;
        KeyStoreKeyFactory factory = new KeyStoreKeyFactory(keyStoreResource, jksPassword.toCharArray());
        return factory.getKeyPair(jksAlias, keyPassword.toCharArray());
    }

    /**
     * 解析密钥库位置：支持 classpath:、file: 前缀，其余按文件路径处理
     */
    private static Resource resolveKeyStoreResource(String location) {
        if (location.startsWith("classpath:")) {
            return new ClassPathResource(location.substring("classpath:".length()));
        }
        if (location.startsWith("file:")) {
            return new FileSystemResource(location.substring("file:".length()));
        }
        return new FileSystemResource(location);
    }

    private static boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
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
