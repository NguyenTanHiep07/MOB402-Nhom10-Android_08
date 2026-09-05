package com.mob10.deliveryserver.controller;

import com.mob10.deliveryserver.dto.AccountDtos.*;
import com.mob10.deliveryserver.service.AccountService;
import com.mob10.deliveryserver.security.AuthenticatedUser;
import jakarta.validation.Valid;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
public class AccountController {
    private final AccountService accounts;
    public AccountController(AccountService accounts) { this.accounts = accounts; }
    @GetMapping("/api/account")
    public Profile profile(@AuthenticationPrincipal AuthenticatedUser user) { return accounts.profile(user.id()); }
    @PutMapping("/api/account")
    public Profile edit(@AuthenticationPrincipal AuthenticatedUser user, @Valid @RequestBody Edit body) {
        return accounts.edit(user.id(), body);
    }
    @PostMapping("/api/account/email/request")
    public Message link(@AuthenticationPrincipal AuthenticatedUser user, @Valid @RequestBody Link body) {
        accounts.link(user.id(), body); return new Message("Yêu cầu gửi mã đã được tiếp nhận. Kiểm tra email vừa nhập và thư rác. Mã có hiệu lực 10 phút.");
    }
    @GetMapping("/api/account/email/status")
    public Message emailStatus(@AuthenticationPrincipal AuthenticatedUser user) { return accounts.emailStatus(user.id()); }
    @PostMapping("/api/account/email/verify")
    public Profile verify(@AuthenticationPrincipal AuthenticatedUser user, @Valid @RequestBody Verify body) {
        return accounts.verify(user.id(), body);
    }
    @PostMapping("/api/auth/recovery/request")
    public Message request(@Valid @RequestBody Request body, HttpServletRequest request) {
        accounts.request(body.phoneNumber(), request.getRemoteAddr());
        return new Message("Nếu số điện thoại có email đã xác minh, mã sẽ được gửi đến email đó. Kiểm tra hộp thư và thư rác. Nếu chưa liên kết email, hãy liên hệ quản trị viên.");
    }
    @PostMapping("/api/auth/recovery/complete")
    public Message reset(@Valid @RequestBody Reset body, HttpServletRequest request) {
        accounts.reset(body, request.getRemoteAddr());
        return new Message("Đổi mật khẩu thành công. Hãy đăng nhập bằng tên đăng nhập và mật khẩu mới.");
    }
}
