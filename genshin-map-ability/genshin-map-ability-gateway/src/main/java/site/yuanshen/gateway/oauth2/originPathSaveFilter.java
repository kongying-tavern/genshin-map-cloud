package site.yuanshen.gateway.oauth2;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import site.yuanshen.gateway.config.GenshinGatewayProperties;

/**
 * TODO
 *
 * @author Moment
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class originPathSaveFilter implements GlobalFilter, Ordered {

    private final GenshinGatewayProperties genshinGatewayProperties;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        //请求url
        String path = exchange.getRequest().getPath().value();
        ServerHttpRequest request = exchange.getRequest().mutate()
                .header("originalPath", path).build();
        return chain.filter(exchange.mutate().request(request).build());
    }

    @Override
    public int getOrder() {
        return 0;
    }
}
