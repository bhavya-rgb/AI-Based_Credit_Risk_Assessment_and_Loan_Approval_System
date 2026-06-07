package com.creditrisk.config;

import com.creditrisk.auth.AuthService;
import com.creditrisk.user.RoleEntity;
import com.creditrisk.user.RoleName;
import com.creditrisk.user.RoleRepository;
import com.creditrisk.user.UserEntity;
import com.creditrisk.user.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.transaction.annotation.Transactional;

@Configuration
public class SeedDataRunner {

    @Bean
    @Order(10)
    CommandLineRunner seedAdmin(AuthService authService,
                                RoleRepository roleRepository,
                                UserRepository userRepository,
                                @Value("${app.admin.seed-admin-email}") String adminEmail,
                                @Value("${app.admin.seed-admin-password}") String adminPassword) {
        return args -> seedAdminTx(authService, roleRepository, userRepository, adminEmail, adminPassword);
    }

    @Transactional
    void seedAdminTx(AuthService authService, RoleRepository roleRepository, UserRepository userRepository,
                     String adminEmail, String adminPassword) {
        UserEntity admin = authService.ensureSeedAdmin(adminEmail, "System Admin", adminPassword);
        boolean hasAdminRole = admin.getRoles().stream().anyMatch(r -> r.getName() == RoleName.ADMIN);
        if (!hasAdminRole) {
            RoleEntity adminRole = roleRepository.findByName(RoleName.ADMIN).orElseThrow();
            admin.getRoles().add(adminRole);
            userRepository.save(admin);
        }
    }
}
