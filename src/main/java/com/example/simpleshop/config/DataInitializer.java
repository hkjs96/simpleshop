package com.example.simpleshop.config;

import com.example.simpleshop.domain.user.User;
import com.example.simpleshop.domain.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
@RequiredArgsConstructor
public class DataInitializer {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Bean
    public CommandLineRunner initUsers() {
        return args -> {
            if (userRepository.count() == 0) {
                userRepository.save(User.builder()
                        .email("alice@example.com")
                        .password(passwordEncoder.encode("password123"))
                        .nickname("Alice")
                        .build());

                userRepository.save(User.builder()
                        .email("bob@example.com")
                        .password(passwordEncoder.encode("password123"))
                        .nickname("Bob")
                        .build());

                userRepository.save(User.builder()
                        .email("charlie@example.com")
                        .password(passwordEncoder.encode("password123"))
                        .nickname("Charlie")
                        .build());

                System.out.println("✅ 기본 사용자 3명 등록 완료");
            }
        };
    }
}
