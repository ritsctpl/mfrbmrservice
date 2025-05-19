package com.rits.apigateway.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.SecurityWebFiltersOrder;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.web.cors.reactive.CorsWebFilter;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import org.springframework.web.server.ServerWebExchange;

import java.util.List;


@Configuration
@Import(CorsConfig.class)
@EnableWebFluxSecurity
public class SecurityConfig {
    @Autowired
    private CorsWebFilter corsWebFilter;
   /* @Bean
    public SecurityWebFilterChain springSecurityFilterChain(ServerHttpSecurity serverHttpSecurity){
        serverHttpSecurity.csrf()
                .disable()
                .addFilterAt(corsWebFilter, SecurityWebFiltersOrder.CORS)
                .authorizeExchange(exchange -> exchange
                        .pathMatchers("/eureka/**")
                        .permitAll()
                        .anyExchange()
                        .authenticated())
                .oauth2ResourceServer(ServerHttpSecurity.OAuth2ResourceServerSpec::jwt);
        return serverHttpSecurity.build();

    }*/

    @Value("${TRUSTED_ALLOWLISTED_IPS}")
    private List<String> allowlistedIps; // Trusted IPs from application.properties

    @Bean
    public SecurityWebFilterChain springSecurityFilterChain(ServerHttpSecurity serverHttpSecurity) {
        serverHttpSecurity.csrf()
                .disable()
                .addFilterAt(corsWebFilter, SecurityWebFiltersOrder.CORS)
                .addFilterBefore(ipAllowlistFilter(), SecurityWebFiltersOrder.AUTHENTICATION)
                .authorizeExchange(exchange -> exchange
                        .pathMatchers("/eureka/**").permitAll() // Public endpoint for Eureka
                        .pathMatchers("/public/**").permitAll() // Allowlisted endpoints, e.g., accessible for SAP MII
                        .anyExchange().authenticated())         // JWT authentication for sensitive endpoints
                .oauth2ResourceServer(ServerHttpSecurity.OAuth2ResourceServerSpec::jwt);
        return serverHttpSecurity.build();
    }

    // Define a WebFilter to allowlist specific IP addresses for certain paths
    @Bean
    public WebFilter ipAllowlistFilter() {
        return (ServerWebExchange exchange, WebFilterChain chain) -> {
            String clientIp = exchange.getRequest().getRemoteAddress().getAddress().getHostAddress();

            // Allowlist IPs for the /public path
            if (exchange.getRequest().getURI().getPath().startsWith("/public")) {
                if (allowlistedIps.contains(clientIp)) {
                    return chain.filter(exchange);
                } else {
                    exchange.getResponse().setStatusCode(org.springframework.http.HttpStatus.FORBIDDEN);
                    return exchange.getResponse().setComplete();
                }
            }

            // For other endpoints, proceed to regular security checks
            return chain.filter(exchange);
        };
    }
}
