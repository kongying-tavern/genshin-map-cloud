package site.yuanshen.gateway.oauth2;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusReactiveJwtDecoder;
import org.springframework.security.web.server.SecurityWebFilterChain;

/**
 * 网关通过的Security配置
 *
 * @author Moment
 */
@EnableWebFluxSecurity
@Configuration
public class WebFluxSecurityConfiguration {

    @Value("${spring.security.oauth2.resourceserver.jwt.jwk-set-uri}")
    String jwkSetUri;

    @Bean
    SecurityWebFilterChain securityFilterChain(ServerHttpSecurity http) {
        http.csrf().disable();
        http.authorizeExchange()
                .pathMatchers("/oauth/**").permitAll()
                .pathMatchers("/api/**").authenticated()
                .anyExchange().permitAll();
        //oauth2资源服务器验证
        http.oauth2ResourceServer().jwt().jwtDecoder(jwtReactiveDecoder());
        return http.build();
    }

    @Bean
    NimbusReactiveJwtDecoder jwtReactiveDecoder() {
        return NimbusReactiveJwtDecoder.withJwkSetUri(this.jwkSetUri).build();
    }

    @Bean
    NimbusJwtDecoder jwtDecoder() {
        return NimbusJwtDecoder.withJwkSetUri(this.jwkSetUri).build();
    }


}
