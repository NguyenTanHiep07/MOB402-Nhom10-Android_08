package com.mob10.deliveryserver.service;

import com.mob10.deliveryserver.domain.User;
import com.mob10.deliveryserver.dto.AuthDtos.*;
import com.mob10.deliveryserver.exception.ApiException;
import com.mob10.deliveryserver.repository.UserRepository;
import com.mob10.deliveryserver.security.JwtService;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {
    private final UserRepository users;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final DtoMapper mapper;

    public AuthService(UserRepository users, PasswordEncoder passwordEncoder, JwtService jwtService, DtoMapper mapper) {
        this.users = users; this.passwordEncoder = passwordEncoder; this.jwtService = jwtService; this.mapper = mapper;
    }

    public LoginResponse login(LoginRequest request) {
        User user = users.findByUsername(request.username().trim())
                .orElseThrow(() -> invalidCredentials());
        if (!user.isActive() || !passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw invalidCredentials();
        }
        return new LoginResponse(jwtService.createToken(user), "Bearer", jwtService.getExpirationMs(), mapper.toUserSummary(user));
    }

    private ApiException invalidCredentials() {
        return new ApiException(HttpStatus.UNAUTHORIZED, "INVALID_CREDENTIALS", "Tên đăng nhập hoặc mật khẩu không đúng");
    }
}
