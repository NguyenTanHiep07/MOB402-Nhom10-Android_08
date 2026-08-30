package com.mob10.deliveryserver.controller;

import com.mob10.deliveryserver.config.SecurityConfig;
import com.mob10.deliveryserver.security.JwtAuthenticationFilter;
import com.mob10.deliveryserver.security.RestAccessDeniedHandler;
import com.mob10.deliveryserver.security.RestAuthenticationEntryPoint;
import com.mob10.deliveryserver.service.LocationService;
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
import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = LocationController.class)
@Import({SecurityConfig.class, RestAuthenticationEntryPoint.class, RestAccessDeniedHandler.class,
        LocationControllerTest.PassThroughJwtFilterConfiguration.class})
class LocationControllerTest {
    @Autowired MockMvc mvc;
    @MockitoBean LocationService locationService;

    @Test
    @WithMockUser(roles = "CLIENT")
    void clientCanAutocompleteAddress() throws Exception {
        when(locationService.autocomplete("Nguyễn Trãi", 6)).thenReturn(List.of());

        mvc.perform(get("/api/locations/autocomplete").param("query", "Nguyễn Trãi"))
                .andExpect(status().isOk())
                .andExpect(content().json("[]"));
    }

    @Test
    @WithMockUser(roles = "DELIVERY")
    void deliveryRoleCannotUseClientLocationEndpoint() throws Exception {
        mvc.perform(get("/api/locations/autocomplete").param("query", "Nguyễn Trãi"))
                .andExpect(status().isForbidden());
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
