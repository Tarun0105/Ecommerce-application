package com.shopverse.config;

import com.shopverse.entity.User;
import com.shopverse.entity.UserRole;
import com.shopverse.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements ApplicationRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.admin.email}")
    private String adminEmail;

    @Value("${app.admin.password}")
    private String adminPassword;

    @Override
    public void run(ApplicationArguments args) {
        if (!userRepository.existsByEmail(adminEmail)) {
            User admin = User.builder()
                    .firstName("Admin")
                    .lastName("ShopVerse")
                    .email(adminEmail)
                    .passwordHash(passwordEncoder.encode(adminPassword))
                    .role(UserRole.ROLE_ADMIN)
                    .enabled(true)
                    .build();
            userRepository.save(admin);
            log.info("Default admin account created: {}", adminEmail);
        } else {
            log.info("Admin account already exists: {}", adminEmail);
        }
    }
}
