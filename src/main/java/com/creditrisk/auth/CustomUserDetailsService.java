package com.creditrisk.auth;

import com.creditrisk.user.UserEntity;
import com.creditrisk.user.UserRepository;
import com.creditrisk.user.UserStatus;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class CustomUserDetailsService implements UserDetailsService {
    private final UserRepository userRepository;

    public CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        UserEntity user = userRepository.findByEmailIgnoreCase(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
        return new SecurityPrincipal(
                user.getId(),
                user.getEmail(),
                user.getPasswordHash(),
                user.getStatus() == UserStatus.ACTIVE,
                user.getRoles().stream().map(r -> new SimpleGrantedAuthority("ROLE_" + r.getName().name())).toList()
        );
    }
}
