package com.authplatform.client.security;

import com.authplatform.client.config.AuthClientProperties;
import com.authplatform.client.dto.TokenValidationRequest;
import com.authplatform.client.dto.TokenValidationResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Arrays;
import java.util.Collections;
import java.util.Set;

@Aspect
public class SecurityAspect {

    private final AuthClientProperties properties;
    private final RestTemplate restTemplate;

    public SecurityAspect(AuthClientProperties properties) {
        this.properties = properties;
        this.restTemplate = new RestTemplate();
    }

    @Before("@annotation(requiresProjectRole)")
    public void checkPermission(RequiresProjectRole requiresProjectRole) throws IllegalAccessException {
        // Lấy request hiện tại để đọc header
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
        String authHeader = request.getHeader("Authorization");

        // Kiểm tra và lấy token
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new IllegalAccessException("Missing or invalid Authorization header.");
        }
        String token = authHeader.substring(7);

        // Chuẩn bị và gọi đến API /validate-token của dịch vụ chính
        String validationUrl = String.format("%s/api/p/%s/auth/validate-token", properties.getBaseUrl(), properties.getApiKey());

        TokenValidationRequest validationRequest = new TokenValidationRequest();
        validationRequest.setToken(token);

        TokenValidationResponse response;
        try {
            response = restTemplate.postForObject(validationUrl, validationRequest, TokenValidationResponse.class);
        } catch (HttpClientErrorException.Unauthorized e) {
            throw new IllegalAccessException("Token is invalid or expired.");
        }

        // Kiểm tra kết quả trả về
        if (response == null || !response.isValid()) {
            throw new IllegalAccessException("Token validation failed.");
        }

        // Kiểm tra vai trò (Role)
        Set<String> userRoles = response.getRoles() != null ? response.getRoles() : Collections.emptySet();
        boolean hasPermission = Arrays.stream(requiresProjectRole.value())
                .anyMatch(userRoles::contains);

        if (!hasPermission) {
            throw new AccessDeniedException("User does not have the required roles: " + Arrays.toString(requiresProjectRole.value()));
        }

        request.setAttribute("validatedUserEmail", response.getEmail());
        request.setAttribute("validatedUserId", response.getUserId());
    }
}

class AccessDeniedException extends RuntimeException {
    public AccessDeniedException(String message) { super(message); }
}
