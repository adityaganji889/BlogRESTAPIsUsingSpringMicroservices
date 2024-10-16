package com.services.APIGateway.config;

import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
//import org.springframework.web.server.ServerWebExchange;

import com.services.APIGateway.services.TokenStore;

//import reactor.core.publisher.Mono;

@Component
public class JwtHeaderFilter extends AbstractGatewayFilterFactory<JwtHeaderFilter.Config> {

	private final TokenStore tokenStore;
	
    public JwtHeaderFilter(TokenStore tokenStore) {
        super(Config.class);
        this.tokenStore = tokenStore;
    }

    public static class Config {
        // You can add configuration properties here if needed
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            String jwtToken = exchange.getRequest().getHeaders().getFirst("Authorization");
            
         // If the token is not present, check if it's stored for the user
            if (jwtToken == null) {
                String username = exchange.getRequest().getHeaders().getFirst("X-Username"); // Assuming username is sent
                if (username != null) {
                    jwtToken = tokenStore.getToken(username);
                }
            }
            // You might want to check if jwtToken is not null or empty
            if (jwtToken != null) {
                // Forward the token in the headers of the downstream request
                return chain.filter(
                        exchange.mutate()
                                .request(exchange.getRequest().mutate()
                                        .header(HttpHeaders.AUTHORIZATION, jwtToken)
                                        .build())
                                .build()
                );
            }

            return chain.filter(exchange);
        };
    }
}