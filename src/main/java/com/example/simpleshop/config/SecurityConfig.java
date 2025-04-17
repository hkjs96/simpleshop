package com.example.simpleshop.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(AbstractHttpConfigurer::disable)
            .authorizeHttpRequests(authorize -> authorize
                // Swagger UI endpoints
                .requestMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll()
                // H2 console if needed during development
                .requestMatchers("/h2-console/**").permitAll()
                // Public endpoints
                .requestMatchers("/api/users/signup", "/api/users/login").permitAll()
                // Secure all other endpoints
                .anyRequest().authenticated()
            )
            .logout(logout -> logout
                    .logoutUrl("/api/users/logout")
                    .permitAll()
            )
            .sessionManagement(session -> session
                    .maximumSessions(1) // 동시에 로그인 1개 제한 (선택)
            );

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }
}
