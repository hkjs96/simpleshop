package com.example.simpleshop.config;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.session.HttpSessionEventPublisher;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final SessionAuthenticationFilter sessionAuthenticationFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            // CSRF configuration - disabled for API endpoints but could be enabled with proper configuration
            .csrf(AbstractHttpConfigurer::disable)
            
            // Request authorization rules
                .authorizeHttpRequests(auth -> auth
                        // Swagger & H2 콘솔은 전체 허용
                        .requestMatchers("/swagger-ui/**", "/v3/api-docs/**", "/h2-console/**").permitAll()
                        // 회원가입/로그인은 인증 없이 허용
                        .requestMatchers("/api/users/signup", "/api/users/login").permitAll()
                        // 상품 목록 및 상세 조회 (GET 요청) 전체 공개
                        .requestMatchers(HttpMethod.GET, "/api/products/**").permitAll()
                        // 나머지 API 요청은 인증 필요 (상품 등록/수정/삭제, 마이페이지 등)
                        .requestMatchers("/api/**").authenticated()
                        // 나머지 모든 요청은 인증 필요
                        .anyRequest().authenticated()
                )

                // Session management configuration
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
                    .maximumSessions(1)
                    .expiredUrl("/login?expired")
            )
            
            // Custom filter for session-based authentication
            .addFilterBefore(sessionAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
            
            // Disable form login and HTTP Basic
            .formLogin(AbstractHttpConfigurer::disable)
            .httpBasic(AbstractHttpConfigurer::disable)
            
            // Logout configuration
            .logout(logout -> logout
                .logoutUrl("/api/users/logout")
                .invalidateHttpSession(true)
                .deleteCookies("JSESSIONID")
                .clearAuthentication(true)
                .logoutSuccessHandler((request, response, authentication) -> {
                    response.setContentType("application/json");
                    response.setCharacterEncoding("UTF-8");

                    String json = """
                                {
                                  "success": true,
                                  "data": [],
                                  "message": "로그아웃 성공"
                                }
                                """;

                    response.setStatus(HttpServletResponse.SC_OK);
                    response.getWriter().write(json);
                })
            );

        // For H2 console (development only)
        http.headers(headers ->
                headers.frameOptions(HeadersConfigurer.FrameOptionsConfig::disable)
        );

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }
    
    @Bean
    public HttpSessionEventPublisher httpSessionEventPublisher() {
        return new HttpSessionEventPublisher();
    }
}

