package com.authplatform.client.config;

import com.authplatform.client.resolver.AuthTokenArgumentResolver;
import com.authplatform.client.security.SecurityAspect;
import com.authplatform.client.service.AuthClient;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import java.util.List; // Import

@Configuration
@ConditionalOnWebApplication
@EnableConfigurationProperties(AuthClientProperties.class)
public class AuthClientAutoConfiguration implements WebMvcConfigurer {

    @Bean
    public SecurityAspect securityAspect(AuthClientProperties properties) {
        return new SecurityAspect(properties);
    }

    @Bean
    public AuthClient authClient(AuthClientProperties properties) {
        return new AuthClient(properties);
    }

    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
        resolvers.add(new AuthTokenArgumentResolver());
    }
}