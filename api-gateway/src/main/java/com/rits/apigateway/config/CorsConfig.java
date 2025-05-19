package com.rits.apigateway.config;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsWebFilter;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;
import java.util.Arrays;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
@Configuration
public class CorsConfig {
    private static final Logger logger = LoggerFactory.getLogger(CorsConfig.class);
    @Value("${DOCKER_HOST_IP:106.51.104.117}")
    private String dockerHostIp;

    @Value("${DOCKER_HOST_PORT:8686}")
    private String dockerHostPort;

    @Value("${DOCKER_HOST_API_PORT:8080}")
    private String dockerHostApiPort;
    @Bean
    public CorsWebFilter corsWebFilter() {

        String dockerhost ="http://"+dockerHostIp+":"+dockerHostPort;
        String dockerhostApi ="http://"+dockerHostIp+":"+dockerHostApiPort;
        CorsConfiguration corsConfig = new CorsConfiguration();
        logger.info("Using Docker host IP: {}", dockerHostIp);
        logger.info("Using Docker host: {}", dockerhost);
      //  corsConfig.addAllowedOrigin("*");
        corsConfig.addAllowedMethod("*");
        corsConfig.addAllowedHeader("*");
        //List<String> allowOrigins = Arrays.asList("http://localhost:8989", "http://localhost:8585","http://192.168.1.64:8585","http://192.168.1.64:8080");
        List<String> allowOrigins = Arrays.asList("http://localhost:8989", "http://localhost:8686", "http://localhost:8585","http://localhost:3000","http://192.168.1.64:8080",dockerhost,dockerhostApi);
       // corsConfig.addAllowedOrigin("http://localhost:8989");
        corsConfig.setAllowedOrigins(allowOrigins);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", corsConfig);

        return new CorsWebFilter(source);
    }
}
