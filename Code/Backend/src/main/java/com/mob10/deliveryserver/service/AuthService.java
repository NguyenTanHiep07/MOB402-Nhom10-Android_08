package com.mob10.deliveryserver.service;

import com.mob10.deliveryserver.domain.User;
import com.mob10.deliveryserver.domain.Role;
import com.mob10.deliveryserver.dto.AuthDtos.*;
import com.mob10.deliveryserver.exception.ApiException;
import com.mob10.deliveryserver.repository.UserRepository;
import com.mob10.deliveryserver.security.JwtService;
import org.springframework.http.HttpStatus;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

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
        String rawPhone = request.phoneNumber().trim();
        String normalizedPhone = normalizePhone(rawPhone);
        Optional<User> matched = users.findByPhoneNumber(normalizedPhone);
        if (matched.isEmpty() && !rawPhone.equals(normalizedPhone)) {
            matched = users.findByPhoneNumber(rawPhone);
        }
        User user = matched.orElseThrow(() -> invalidCredentials());
        if (!user.isActive() || !passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw invalidCredentials();
        }
        return new LoginResponse(jwtService.createToken(user), "Bearer", jwtService.getExpirationMs(), mapper.toUserSummary(user));
    }

    public RegisterResponse register(RegisterRequest request) {
        String rawPhone = request.phoneNumber().trim();
        String phone = normalizePhone(rawPhone);
        if (request.password().getBytes(StandardCharsets.UTF_8).length > 72) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "PASSWORD_TOO_LONG",
                    "Mật khẩu không được vượt quá 72 byte UTF-8");
        }
        if (users.existsByPhoneNumber(phone) || users.existsByUsername(phone)
                || (!rawPhone.equals(phone) && users.existsByPhoneNumber(rawPhone))) {
            throw duplicatePhone();
        }
        User user = new User(phone, passwordEncoder.encode(request.password()),
                request.fullName().trim(), phone, Role.CLIENT, null);
        user.setId(sequences.generateSequence("users"));
        try {
            users.save(user);
        } catch (DuplicateKeyException ex) {
            throw duplicatePhone();
        }
        return new RegisterResponse(user.getId(), user.getPhoneNumber(), user.getFullName(), user.getRole());
    }

    private ApiException duplicatePhone() {
        return new ApiException(HttpStatus.CONFLICT, "PHONE_ALREADY_REGISTERED", "Số điện thoại đã được đăng ký");
    }

    private String normalizePhone(String phone) {
        String trimmed = phone.trim();
        return trimmed.startsWith("+84") ? "0" + trimmed.substring(3) : trimmed;
    }

    private ApiException invalidCredentials() {
        return new ApiException(HttpStatus.UNAUTHORIZED, "INVALID_CREDENTIALS", "Số điện thoại hoặc mật khẩu không đúng");
    }
}
