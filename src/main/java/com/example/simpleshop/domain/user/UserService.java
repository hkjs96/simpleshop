package com.example.simpleshop.domain.user;

import com.example.simpleshop.dto.user.*;
import com.example.simpleshop.dto.common.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public ApiResponse<UserResponse> signup(UserSignupRequest request) {
        if (userRepository.findByEmail(request.email()).isPresent()) {
            throw new IllegalArgumentException("이미 사용 중인 이메일입니다.");
        }

        User user = User.builder()
                .email(request.email())
                .password(passwordEncoder.encode(request.password()))
                .nickname(request.nickname())
                .build();

        userRepository.save(user);
        UserResponse userResponse = new UserResponse(user.getId(), user.getEmail(), user.getNickname());
        return ApiResponse.success(userResponse);
    }

    @Transactional(readOnly = true)
    public ApiResponse<UserResponse> login(UserLoginRequest loginRequest) {
        User user = userRepository.findByEmail(loginRequest.email())
                .orElseThrow(() -> new UsernameNotFoundException("존재하지 않는 이메일입니다."));

        if (!passwordEncoder.matches(loginRequest.password(), user.getPassword())) {
            throw new BadCredentialsException("비밀번호가 일치하지 않습니다.");
        }

        UserResponse userResponse = new UserResponse(user.getId(), user.getEmail(), user.getNickname());
        return ApiResponse.success(userResponse);
    }
}
