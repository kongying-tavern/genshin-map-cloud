package site.yuanshen.gateway.oauth2;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import site.yuanshen.data.enums.RoleEnum;
import site.yuanshen.gateway.config.GenshinGatewayProperties;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * TODO
 *
 * @author Moment
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SecurityFilter implements GlobalFilter, Ordered {

    private final NimbusJwtDecoder jwtDecoder;

    private final GenshinGatewayProperties genshinGatewayProperties;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        //header里的身份信息
        String authoritiesString = exchange.getRequest().getHeaders().getFirst("Authorities");
        if (authoritiesString == null || "".equals(authoritiesString)) {
            log.info("Authorities is empty, pass");
            return chain.filter(exchange);
        }
        JSONArray authorities = JSON.parseArray(authoritiesString);
        List<RoleEnum> userRoleList = authorities.toJavaList(String.class).stream().map(RoleEnum::valueOf).collect(Collectors.toList());
        //请求url
        String path = exchange.getRequest().getHeaders().getFirst("originalPath");
        //配置文件中的url身份配置
        Map<String, List<String>> authoritiesFilter = genshinGatewayProperties.getAuthoritiesFilter();
        log.info("Try to match security url map: {}", authoritiesFilter.toString());
        log.info("The user authorities list is: {}", authorities);
        for (String roleName : authoritiesFilter.keySet()) {
            AntPathMatcher matcher = new AntPathMatcher();
            RoleEnum matchRole = RoleEnum.valueOf(roleName);
            List<String> urlMatches = authoritiesFilter.get(roleName);
            for (String urlMatch : urlMatches) {
                log.info("Url matched: {}; need: {}", urlMatch, roleName);
                for (RoleEnum userRole : userRoleList) {
                    if (userRole.getSort() <= matchRole.getSort()) return chain.filter(exchange);
                }
            }
        }
        log.info("Has no permissions, reject");
        return exchange.getResponse().setComplete();
    }

    @Override
    public int getOrder() {
        return 100;
    }
}
