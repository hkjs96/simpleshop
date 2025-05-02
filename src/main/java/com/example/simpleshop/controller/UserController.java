package com.example.simpleshop.controller;

import com.example.simpleshop.domain.user.UserService;
import com.example.simpleshop.dto.user.*;
import com.example.simpleshop.dto.common.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "회원 API", description = "회원가입 / 로그인 / 로그아웃")
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @Operation(summary = "회원가입", description = "이메일, 비밀번호, 닉네임을 입력받아 회원가입합니다.")
    @PostMapping("/signup")
    public ResponseEntity<ApiResponse<UserResponse>> signup(@Valid @RequestBody UserSignupRequest request) {
        return ResponseEntity.ok(userService.signup(request));
    }

    @Operation(summary = "로그인", description = "세션 기반 로그인")
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<UserResponse>> login(@Valid @RequestBody UserLoginRequest request,
                                                           HttpServletRequest httpRequest) {
        ApiResponse<UserResponse> response = userService.login(request);

        // 로그인 성공 시 세션에 사용자 ID 저장
        httpRequest.getSession(true).setAttribute("USER_ID", response.getData().id());

        return ResponseEntity.ok(response);
    }


    @Operation(summary = "로그아웃")
    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<String>> logout(HttpServletRequest request) {
        return ResponseEntity.ok(ApiResponse.success("로그아웃 성공"));
    }
}
