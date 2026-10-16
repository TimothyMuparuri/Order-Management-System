package com.oms.gateway;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
public class AuthGatewayFilter implements GlobalFilter, Ordered {

    private final boolean enabled;
    private final String header;
    private final String apiKey;

    public AuthGatewayFilter(
            @Value("${oms.auth.enabled:false}") boolean enabled,
            @Value("${oms.auth.header:X-API-Key}") String header,
            @Value("${oms.auth.api-key:dev-key}") String apiKey
    ) {
        this.enabled = enabled;
        this.header = header;
        this.apiKey = apiKey;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        if (!enabled) {
            return chain.filter(exchange);
        }
        String path = exchange.getRequest().getURI().getPath();
        if (path.startsWith("/actuator")) {
            return chain.filter(exchange);
        }
        String provided = exchange.getRequest().getHeaders().getFirst(header);
        if (apiKey.equals(provided)) {
            return chain.filter(exchange);
        }
        exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
        return exchange.getResponse().setComplete();
    }

    @Override
    public int getOrder() {
        return -100;
    }
}
