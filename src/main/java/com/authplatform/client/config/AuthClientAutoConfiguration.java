package com.authplatform.client.config;

import com.authplatform.client.security.SecurityAspect;
import com.authplatform.client.service.AuthClient;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnWebApplication
@EnableConfigurationProperties(AuthClientProperties.class)
public class AuthClientAutoConfiguration {

    @Bean
    public SecurityAspect securityAspect(AuthClientProperties properties) {
        return new SecurityAspect(properties);
    }

    @Bean
    public AuthClient authClient(AuthClientProperties properties) {
        return new AuthClient(properties);
    }
}
