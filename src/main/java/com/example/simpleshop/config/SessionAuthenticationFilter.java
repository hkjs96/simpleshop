package com.example.simpleshop.config;

import com.example.simpleshop.domain.user.User;
import com.example.simpleshop.domain.user.UserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class SessionAuthenticationFilter extends OncePerRequestFilter {

    private final UserRepository userRepository;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        // Skip authentication for public endpoints
        if (shouldNotFilter(request)) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            // Get session without creating a new one if it doesn't exist
            HttpSession session = request.getSession(false);
            
            // If there's no session or authentication is already set, continue the filter chain
            if (session == null || SecurityContextHolder.getContext().getAuthentication() != null) {
                filterChain.doFilter(request, response);
                return;
            }

            // Get user ID from session
            Long userId = (Long) session.getAttribute("USER_ID");
            if (userId == null) {
                filterChain.doFilter(request, response);
                return;
            }

            // Find user by ID
            Optional<User> userOptional = userRepository.findById(userId);
            if (userOptional.isEmpty()) {
                // Invalid user ID in session, invalidate session
                session.invalidate();
                filterChain.doFilter(request, response);
                return;
            }

            User user = userOptional.get();
            
            // Create authentication token with user authorities
            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(
                            user, 
                            null, 
                            List.of(new SimpleGrantedAuthority("ROLE_USER"))
                    );

            // Set authentication details
            authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            
            // Set authentication in security context
            SecurityContextHolder.getContext().setAuthentication(authentication);
            
            // Update session last accessed time
            session.setAttribute("LAST_ACCESS", System.currentTimeMillis());
            
        } catch (Exception e) {
            log.error("Cannot set user authentication: {}", e.getMessage());
        }

        filterChain.doFilter(request, response);
    }
    
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getServletPath();
        return path.startsWith("/swagger-ui") || 
               path.startsWith("/v3/api-docs") || 
               path.startsWith("/h2-console") ||
               path.equals("/api/users/signup") || 
               path.equals("/api/users/login");
    }
}

