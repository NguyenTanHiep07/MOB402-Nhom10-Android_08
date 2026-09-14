package com.mob10.deliveryserver.security;

import com.mob10.deliveryserver.config.SecurityConfig;
import com.mob10.deliveryserver.controller.OrderController;
import com.mob10.deliveryserver.controller.AuthController;
import com.mob10.deliveryserver.domain.Role;
import com.mob10.deliveryserver.dto.AuthDtos.RegisterResponse;
import com.mob10.deliveryserver.service.AuthService;
import com.mob10.deliveryserver.dto.OrderDtos.CreateOrderRequest;
import com.mob10.deliveryserver.service.OrderService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.io.IOException;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = {OrderController.class, AuthController.class})
@Import({SecurityConfig.class, RestAuthenticationEntryPoint.class, RestAccessDeniedHandler.class,
        SecurityConfigTest.PassThroughJwtFilterConfiguration.class})
class SecurityConfigTest {
    @Autowired MockMvc mvc;
    @MockitoBean OrderService orderService;
    @MockitoBean AuthService authService;

    @Test
    void customerRegistrationIsPublicAndCannotChooseRole() throws Exception {
        when(authService.register(any())).thenReturn(
                new RegisterResponse(12L, "0901234567", "Khách mới", Role.CLIENT));
        mvc.perform(post("/api/auth/register")
                        .contentType("application/json")
                        .content("""
                                {"phoneNumber":"0901234567","password":"abcdef12",
                                 "fullName":"Khách mới","role":"ADMIN"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.role").value("CLIENT"));
    }

    @Test
    void registrationRejectsInvalidPhone() throws Exception {
        mvc.perform(post("/api/auth/register")
                        .contentType("application/json")
                        .content("""
                                {"phoneNumber":"123","password":"abcdef12","fullName":"Khách mới"}
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void unauthenticatedRequestReturnsJson401() throws Exception {
        mvc.perform(get("/api/orders"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("UNAUTHORIZED"))
                .andExpect(jsonPath("$.path").value("/api/orders"));
    }

    @Test
    @WithMockUser(roles = "DELIVERY")
    void deliveryRoleCannotCreateClientOrder() throws Exception {
        mvc.perform(post("/api/orders")
                        .contentType("application/json")
                        .content("{}"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("FORBIDDEN"));
    }

    @Test
    @WithMockUser(roles = "CLIENT")
    void clientRoleCanReachCreateOrderEndpoint() throws Exception {
        when(orderService.create(any(), any(CreateOrderRequest.class))).thenReturn(null);
        mvc.perform(post("/api/orders")
                        .contentType("application/json")
                        .content("""
                                {
                                  "pickupAddress":"A",
                                  "deliveryAddress":"B",
                                  "pickupLatitude":10.7769,
                                  "pickupLongitude":106.7009,
                                  "deliveryLatitude":10.7827,
                                  "deliveryLongitude":106.6958,
                                  "senderName":"Sender",
                                  "senderPhone":"0901234567",
                                  "recipientName":"Recipient",
                                  "recipientPhone":"0987654321",
                                  "distanceKm":1,
                                  "packages":[{"name":"Box","weightKg":1,"quantity":1,"fragile":false,"express":false}]
                                }
                                """))
                .andExpect(status().isCreated());
    }

    @TestConfiguration
    static class PassThroughJwtFilterConfiguration {
        @Bean
        JwtAuthenticationFilter jwtAuthenticationFilter() {
            return new JwtAuthenticationFilter(null, null) {
                @Override
                protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                                FilterChain chain) throws ServletException, IOException {
                    chain.doFilter(request, response);
                }
            };
        }
    }
}
