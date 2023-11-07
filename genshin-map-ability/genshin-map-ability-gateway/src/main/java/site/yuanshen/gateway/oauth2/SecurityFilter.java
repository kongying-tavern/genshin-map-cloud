package site.yuanshen.gateway.oauth2;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import site.yuanshen.common.core.exception.GenshinApiException;
import site.yuanshen.data.enums.RoleEnum;
import site.yuanshen.gateway.config.GenshinGatewayProperties;

import java.util.Comparator;
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
        if (path == null) throw new GenshinApiException("请求url拦截失败，请联系管理员");
        //配置文件中的url身份配置
        Map<String, List<String>> authoritiesFilter = genshinGatewayProperties.getAuthoritiesFilter();
        log.debug("Try to match security url map: {}", authoritiesFilter.toString());
        log.debug("The user authorities list is: {}", authorities);
        for (String roleName : authoritiesFilter.keySet()) {
            RoleEnum matchRole = RoleEnum.valueOf(roleName);
            boolean isMatch = false;
            //userData直接取最大值,和请求权限无关
            int userDataLevel = userRoleList.stream().map(RoleEnum::getUserDataLevel).max(Comparator.comparing(x->x)).orElse(0);
            for (RoleEnum userRole : userRoleList) {
                if (userRole.getSort() <= matchRole.getSort()) {
                    isMatch = true;
                    break;
                }
            }

            if (!isMatch) continue;
            AntPathMatcher matcher = new AntPathMatcher();
            List<String> urlMatches = authoritiesFilter.get(roleName);
            log.debug("{}'s Url matching: {}", roleName, urlMatches);
            for (String urlMatch : urlMatches) {
                if (matcher.match(urlMatch, path)) {
                    log.info("Url matched: {} , has role, pass", urlMatch);
                    log.info("Url userDataLevel: {}", userDataLevel);
                    exchange.getRequest().mutate().header("userDataLevel", String.valueOf(userDataLevel));
                    return chain.filter(exchange);
                }
            }
            log.debug("{}'s Url no matched, find next", roleName);
        }
        log.info("Has no permissions, reject");
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(HttpStatus.FORBIDDEN);
        return response.setComplete();
    }

    @Override
    public int getOrder() {
        return 100;
    }
}
