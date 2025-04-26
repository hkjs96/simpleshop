package com.example.simpleshop.controller;

import com.example.simpleshop.domain.user.UserService;
import com.example.simpleshop.dto.user.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@Tag(name = "회원 API", description = "회원가입 / 로그인 / 로그아웃")
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @Operation(summary = "회원가입", description = "이메일, 비밀번호, 닉네임을 입력받아 회원가입합니다.")
    @PostMapping("/signup")
    public ResponseEntity<UserResponse> signup(@Valid @RequestBody UserSignupRequest request) {
        return ResponseEntity.ok(userService.signup(request));
    }

    @Operation(summary = "로그인", description = "세션 기반 로그인")
    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(@Valid @RequestBody UserLoginRequest request,
                                        HttpServletRequest servletRequest,
                                        HttpServletResponse servletResponse) {
        // Create a new session and invalidate any existing session (prevents session fixation)
        HttpSession session = servletRequest.getSession(true);
        
        // Authenticate user and set session attributes
        UserResponse userResponse = userService.login(request, servletRequest, servletResponse);

        // Return user info without sensitive data
        Map<String, Object> responseBody = new HashMap<>();
        responseBody.put("message", "로그인 성공");
        responseBody.put("user", userResponse);
        
        return ResponseEntity.ok(responseBody);
    }

    @Operation(summary = "로그아웃", description = "세션을 무효화하여 로그아웃합니다.")
    @PostMapping("/logout")
    public ResponseEntity<Map<String, String>> logout(HttpServletRequest request, HttpServletResponse response) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }
        
        // Clear the session cookie
        Cookie cookie = new Cookie("JSESSIONID", null);
        cookie.setPath("/");
        cookie.setMaxAge(0);
        cookie.setHttpOnly(true);
        cookie.setSecure(request.isSecure()); // Set secure flag if using HTTPS
        response.addCookie(cookie);
        
        Map<String, String> responseBody = new HashMap<>();
        responseBody.put("message", "로그아웃 성공");
        
        return ResponseEntity.ok(responseBody);
    }
    
    // "/me" endpoint has been removed as requested
    

}


