package site.yuanshen.gateway.oauth2;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import site.yuanshen.data.enums.RoleEnum;
import site.yuanshen.gateway.config.GenshinGatewayProperties;

import java.util.List;

/**
 * TODO
 *
 * @author Moment
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class TokenConvertFilter implements GlobalFilter, Ordered {

    private final NimbusJwtDecoder jwtDecoder;
    private final GenshinGatewayProperties genshinGatewayProperties;

    @Value("${env:prd}")
    private String env;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        //判断是否是登录请求
        AntPathMatcher matcher = new AntPathMatcher();
        String path = exchange.getRequest().getHeaders().getFirst("originalPath");
        log.debug("path: " + path);
        boolean isPass = false;
        for (String filter : genshinGatewayProperties.getPassFilter()) {
            if(matcher.match(filter, path)) isPass = true;
        }
        if (isPass) {
            log.debug("match PassFilter, pass");
            return chain.filter(exchange);
        }
        //获取token
        String token = exchange.getRequest().getHeaders().getFirst("Authorization");
        log.debug("token: " + token);
        //token为空
        if (token == null || "".equals(token)) {
            ServerHttpResponse response = exchange.getResponse();
            response.setStatusCode(HttpStatus.FORBIDDEN);
            return response.setComplete();
        }
        //basic token
        if (token.contains("Basic")) {
            log.debug("Basic auth, pass");
            return chain.filter(exchange);
        }
        //解析token
        String realToken = token.replace("Bearer", "");
        Jwt decode = jwtDecoder.decode(realToken);
        Object userNameClaim = decode.getClaim("user_name");
        //无userName，是客户端模式
        if (userNameClaim == null || userNameClaim.toString().equals("")) {
            log.debug("client token, write visitor authority");
            ServerHttpRequest request = exchange.getRequest().mutate()
                    .headers(httpHeaders -> httpHeaders.remove("Authorization"))
                    .header("Authorities", JSON.toJSONString(List.of(RoleEnum.VISITOR.getCode()))).build();
            return chain.filter(exchange.mutate().request(request).build());
        }
        String userName = userNameClaim.toString();
        String authorities = decode.getClaim("authorities").toString();
        String userId = decode.getClaim("userId").toString();
        String env = decode.getClaim("env").toString();
        log.debug("env: {}", env);
        log.debug("userName: " + userName);
        log.debug("authorities: " + authorities);
        log.debug("userId: " + userId);

        if (!this.env.equals(env)) {
            ServerHttpResponse response = exchange.getResponse();
            response.setStatusCode(HttpStatus.FORBIDDEN);
            return response.setComplete();
        }

        final boolean isTestUser = JSONArray.parseArray(authorities, RoleEnum.class).stream().anyMatch(role -> role.getSort() <= RoleEnum.MAP_NEIGUI.getSort());
        ServerHttpRequest request = exchange.getRequest().mutate()
                .headers(httpHeaders -> httpHeaders.remove("Authorization"))
                .header("userName", userName)
                .header("Authorities", authorities)
                .header("isTestUser", isTestUser ? "TEST" : "")
                .header("userId", userId).build();
        return chain.filter(exchange.mutate().request(request).build());
    }

    @Override
    public int getOrder() {
        return 99;
    }
}
