package com.creditrisk.auth;

import com.creditrisk.user.UserEntity;
import com.creditrisk.user.UserRepository;
import com.creditrisk.user.UserStatus;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private final JwtService jwtService;
    private final UserRepository userRepository;

    public JwtAuthenticationFilter(JwtService jwtService, UserRepository userRepository) {
        this.jwtService = jwtService;
        this.userRepository = userRepository;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String header = request.getHeader("Authorization");
        if (header == null || !header.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }
        String token = header.substring(7);
        if (!jwtService.isValid(token)) {
            filterChain.doFilter(request, response);
            return;
        }
        Claims claims = jwtService.parse(token);
        String email = claims.getSubject();
        Optional<UserEntity> userOpt = userRepository.findByEmailIgnoreCase(email);
        if (userOpt.isEmpty()) {
            filterChain.doFilter(request, response);
            return;
        }
        UserEntity user = userOpt.get();
        if (user.getStatus() != UserStatus.ACTIVE) {
            filterChain.doFilter(request, response);
            return;
        }
        SecurityPrincipal principal = new SecurityPrincipal(
                user.getId(),
                user.getEmail(),
                user.getPasswordHash(),
                true,
                user.getRoles().stream().map(r -> new SimpleGrantedAuthority("ROLE_" + r.getName().name())).toList()
        );
        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                principal,
                null,
                principal.getAuthorities()
        );
        authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
        SecurityContextHolder.getContext().setAuthentication(authentication);
        filterChain.doFilter(request, response);
    }
}
