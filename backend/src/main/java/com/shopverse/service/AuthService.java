package com.shopverse.service;

import com.shopverse.dto.request.LoginRequest;
import com.shopverse.dto.request.RegisterRequest;
import com.shopverse.dto.response.AuthResponse;
import com.shopverse.entity.User;
import com.shopverse.entity.UserRole;
import com.shopverse.exception.BadRequestException;
import com.shopverse.exception.DuplicateResourceException;
import com.shopverse.repository.UserRepository;
import com.shopverse.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final AuthenticationManager authenticationManager;

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException("An account with email '" + request.getEmail() + "' already exists.");
        }
        User user = User.builder()
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .email(request.getEmail())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .role(UserRole.ROLE_USER)
                .enabled(true)
                .build();
        user = userRepository.save(user);
        String token = jwtTokenProvider.generateToken(user.getEmail(), user.getRole().name());
        log.info("New user registered: {}", user.getEmail());
        return AuthResponse.builder()
                .token(token).type("Bearer")
                .email(user.getEmail())
                .firstName(user.getFirstName()).lastName(user.getLastName())
                .role(user.getRole().name())
                .build();
    }

    public AuthResponse login(LoginRequest request) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));
        } catch (BadCredentialsException | DisabledException e) {
            throw new BadRequestException("Invalid email or password.");
        }
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new BadRequestException("User not found."));
        if (!user.isEnabled()) {
            throw new BadRequestException("Account is disabled. Please contact support.");
        }
        String token = jwtTokenProvider.generateToken(user.getEmail(), user.getRole().name());
        return AuthResponse.builder()
                .token(token).type("Bearer")
                .email(user.getEmail())
                .firstName(user.getFirstName()).lastName(user.getLastName())
                .role(user.getRole().name())
                .build();
    }
}
