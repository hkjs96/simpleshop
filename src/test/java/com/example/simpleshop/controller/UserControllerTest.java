package com.example.simpleshop.controller;

import com.example.simpleshop.domain.user.UserService;
import com.example.simpleshop.dto.user.UserLoginRequest;
import com.example.simpleshop.dto.user.UserResponse;
import com.example.simpleshop.dto.user.UserSignupRequest;
import com.example.simpleshop.domain.user.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityFilterAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.Cookie;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;

@WebMvcTest(controllers = UserController.class, excludeAutoConfiguration = { SecurityAutoConfiguration.class, SecurityFilterAutoConfiguration.class })
@AutoConfigureMockMvc(addFilters = false)
public class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private UserService userService;

    // SessionAuthenticationFilter 의존성 해결을 위해 UserRepository 모킹 추가
    @MockitoBean
    private UserRepository userRepository;

    // SessionAuthenticationFilter 의존성 해결을 위해 UserRepository 모킹 추가

    private UserSignupRequest signupRequest;
    private UserLoginRequest loginRequest;
    private UserResponse userResponse;
    private static final String SESSION_COOKIE = "JSESSIONID";

    @BeforeEach
    void setUp() {
        signupRequest = new UserSignupRequest("test@example.com", "password123!", "testuser");
        loginRequest = new UserLoginRequest("test@example.com", "password123!");
        userResponse = UserResponse.builder()
                .email(signupRequest.email())
                .nickname(signupRequest.nickname())
                .build();
    }

    @Test
    void login_ShouldReturnMessageAndUser() throws Exception {
        when(userService.login(any(UserLoginRequest.class))).thenReturn(userResponse);

        mockMvc.perform(post("/api/users/login").with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("로그인 성공"))
                .andExpect(jsonPath("$.user.email").value(userResponse.email()));
    }

    @Test
    void logout_ShouldInvalidateSessionAndClearCookie() throws Exception {
        mockMvc.perform(post("/api/users/logout").with(csrf())
                        .cookie(new Cookie(SESSION_COOKIE, "dummy")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("로그아웃 성공"))
                .andExpect(cookie().maxAge(SESSION_COOKIE, 0));
    }
}
