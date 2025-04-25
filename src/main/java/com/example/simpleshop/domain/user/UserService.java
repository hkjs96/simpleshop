package com.example.simpleshop.domain.user;

import com.example.simpleshop.dto.user.*;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.servlet.http.HttpSession;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public UserResponse signup(UserSignupRequest request) {
        if (userRepository.findByEmail(request.email()).isPresent()) {
            throw new IllegalArgumentException("이미 사용 중인 이메일입니다.");
        }

        User user = User.builder()
                .email(request.email())
                .password(passwordEncoder.encode(request.password()))
                .nickname(request.nickname())
                .build();

        userRepository.save(user);

        return new UserResponse(user.getId(), user.getEmail(), user.getNickname());
    }

    @Transactional
    public UserResponse login(UserLoginRequest loginRequest, HttpServletRequest request, HttpServletResponse response) {
        // Find user by email
        User user = userRepository.findByEmail(loginRequest.email())
                .orElseThrow(() -> new UsernameNotFoundException("존재하지 않는 이메일입니다."));

        // Verify password
        if (!passwordEncoder.matches(loginRequest.password(), user.getPassword())) {
            throw new BadCredentialsException("비밀번호가 일치하지 않습니다.");
        }

        HttpSession oldSession = request.getSession(false);
        if (oldSession != null) {
            oldSession.invalidate();
        }

        HttpSession newSession = request.getSession(true);
        
        // Set user ID in session
        newSession.setAttribute("USER_ID", user.getId());
        newSession.setAttribute("LOGIN_TIME", System.currentTimeMillis());
        // Set session timeout (e.g., 30 minutes)
        newSession.setMaxInactiveInterval(30 * 60);

        // Configure session cookie
        configureCookie(response, newSession);

        System.out.println("세션ID: " + newSession.getId());
        
        // Return user info without sensitive data
        return new UserResponse(user.getId(), user.getEmail(), user.getNickname());
    }

    public void logout(HttpSession session) {
        if (session != null) {
            session.invalidate();
        }
    }
    
    // getCurrentUser method has been removed as the /api/users/me endpoint has been removed

    /**
     * Configure the session cookie with secure settings
     */
    private void configureCookie(HttpServletResponse response, HttpSession session) {
        String sessionId = session.getId();

        // Create a secure cookie for the session ID
        Cookie cookie = new Cookie("JSESSIONID", sessionId);
        cookie.setPath("/");
        cookie.setHttpOnly(true); // Prevents JavaScript access to the cookie
        cookie.setMaxAge(-1); // Session cookie (expires when browser closes)

        // In production, set these additional flags
        // cookie.setSecure(true); // Requires HTTPS
        // cookie.setAttribute("SameSite", "Lax"); // Modern browsers support this

        response.addCookie(cookie);
    }
}


