package com.mob10.deliveryserver.service;

import com.mob10.deliveryserver.domain.Role;
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
    private final SequenceGeneratorService sequences;

    public AuthService(UserRepository users, PasswordEncoder passwordEncoder, JwtService jwtService,
                       DtoMapper mapper, SequenceGeneratorService sequences) {
        this.users = users; this.passwordEncoder = passwordEncoder; this.jwtService = jwtService;
        this.mapper = mapper; this.sequences = sequences;
    }

    public LoginResponse login(LoginRequest request) {
        User user = users.findByPhoneNumber(request.phoneNumber().trim())
                .orElseThrow(() -> invalidCredentials());
        if (!user.isActive() || !passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw invalidCredentials();
        }
        return new LoginResponse(jwtService.createToken(user), "Bearer", jwtService.getExpirationMs(), mapper.toUserSummary(user));
    }

    /**
     * Đăng ký tài khoản khách hàng mới.
     * Chỉ cho phép đăng ký vai trò CLIENT. Tài xế và Admin được tạo bởi quản trị viên.
     */
    public LoginResponse register(RegisterRequest request) {
        String username = request.username().trim().toLowerCase();
        String phone = request.phoneNumber().trim();
        String fullName = request.fullName().trim();

        if (users.existsByUsername(username)) {
            throw new ApiException(HttpStatus.CONFLICT, "USERNAME_EXISTS", "Tên đăng nhập đã được sử dụng");
        }
        if (users.existsByPhoneNumber(phone)) {
            throw new ApiException(HttpStatus.CONFLICT, "PHONE_EXISTS", "Số điện thoại đã được đăng ký");
        }

        User user = new User(username, passwordEncoder.encode(request.password()), fullName, phone, Role.CLIENT, null);
        user.setId(sequences.generateSequence("users"));
        users.save(user);

        return new LoginResponse(jwtService.createToken(user), "Bearer", jwtService.getExpirationMs(), mapper.toUserSummary(user));
    }

    private ApiException invalidCredentials() {
        return new ApiException(HttpStatus.UNAUTHORIZED, "INVALID_CREDENTIALS", "Số điện thoại hoặc mật khẩu không đúng");
    }
}

