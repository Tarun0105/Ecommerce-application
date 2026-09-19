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
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    UserRepository userRepository;
    @Mock
    PasswordEncoder passwordEncoder;
    @Mock
    JwtTokenProvider jwtTokenProvider;
    @Mock
    AuthenticationManager authenticationManager;
    @InjectMocks
    AuthService authService;

    // --- register() ---

    @Test
    void register_validRequest_returnsAuthResponse() {
        RegisterRequest req = new RegisterRequest("John", "Doe", "john@test.com", "Password@1");
        when(userRepository.existsByEmail("john@test.com")).thenReturn(false);
        when(passwordEncoder.encode("Password@1")).thenReturn("hashed");
        when(userRepository.save(any(User.class))).thenAnswer(inv -> {
            User u = inv.getArgument(0);
            u.setId(1L);
            return u;
        });
        when(jwtTokenProvider.generateToken("john@test.com", "ROLE_USER")).thenReturn("jwt-token");

        AuthResponse result = authService.register(req);

        assertThat(result.getToken()).isEqualTo("jwt-token");
        assertThat(result.getEmail()).isEqualTo("john@test.com");
        assertThat(result.getFirstName()).isEqualTo("John");
        assertThat(result.getLastName()).isEqualTo("Doe");
        assertThat(result.getRole()).isEqualTo("ROLE_USER");
        assertThat(result.getType()).isEqualTo("Bearer");
        verify(userRepository).save(any(User.class));
        verify(passwordEncoder).encode("Password@1");
    }

    @Test
    void register_duplicateEmail_throwsDuplicateResourceException() {
        RegisterRequest req = new RegisterRequest("John", "Doe", "existing@test.com", "Password@1");
        when(userRepository.existsByEmail("existing@test.com")).thenReturn(true);

        assertThatThrownBy(() -> authService.register(req))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("existing@test.com");
    }

    @Test
    void register_passwordIsEncoded_notStoredPlaintext() {
        RegisterRequest req = new RegisterRequest("Jane", "Smith", "jane@test.com", "plainPass");
        when(userRepository.existsByEmail(any())).thenReturn(false);
        when(passwordEncoder.encode("plainPass")).thenReturn("bcrypt-hash");
        when(userRepository.save(any(User.class))).thenAnswer(inv -> {
            User u = inv.getArgument(0);
            u.setId(2L);
            assertThat(u.getPasswordHash()).isEqualTo("bcrypt-hash");
            return u;
        });
        when(jwtTokenProvider.generateToken(any(), any())).thenReturn("tok");

        authService.register(req);
        verify(passwordEncoder).encode("plainPass");
    }

    @Test
    void register_newUser_hasRoleUser() {
        RegisterRequest req = new RegisterRequest("Alice", "W", "alice@test.com", "Pass@1234");
        when(userRepository.existsByEmail(any())).thenReturn(false);
        when(passwordEncoder.encode(any())).thenReturn("hashed");
        when(userRepository.save(any(User.class))).thenAnswer(inv -> {
            User u = inv.getArgument(0);
            assertThat(u.getRole()).isEqualTo(UserRole.ROLE_USER);
            assertThat(u.isEnabled()).isTrue();
            u.setId(3L);
            return u;
        });
        when(jwtTokenProvider.generateToken(any(), any())).thenReturn("tok");

        authService.register(req);
    }

    // --- login() ---

    @Test
    void login_validCredentials_returnsAuthResponse() {
        LoginRequest req = new LoginRequest("john@test.com", "Password@1");
        User user = User.builder()
                .id(1L).email("john@test.com")
                .firstName("John").lastName("Doe")
                .passwordHash("hashed").role(UserRole.ROLE_USER).enabled(true)
                .build();
        when(userRepository.findByEmail("john@test.com")).thenReturn(Optional.of(user));
        when(jwtTokenProvider.generateToken("john@test.com", "ROLE_USER")).thenReturn("login-token");

        AuthResponse result = authService.login(req);

        assertThat(result.getToken()).isEqualTo("login-token");
        assertThat(result.getEmail()).isEqualTo("john@test.com");
        assertThat(result.getFirstName()).isEqualTo("John");
        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
    }

    @Test
    void login_badCredentials_throwsBadRequestException() {
        LoginRequest req = new LoginRequest("bad@test.com", "wrong");
        doThrow(new BadCredentialsException("Bad credentials"))
                .when(authenticationManager).authenticate(any());

        assertThatThrownBy(() -> authService.login(req))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Invalid email or password");
    }

    @Test
    void login_disabledAccount_throwsBadRequestException() {
        LoginRequest req = new LoginRequest("disabled@test.com", "Pass@1234");
        doThrow(new DisabledException("Account disabled"))
                .when(authenticationManager).authenticate(any());

        assertThatThrownBy(() -> authService.login(req))
                .isInstanceOf(BadRequestException.class);
    }

    @Test
    void login_userNotFoundAfterAuth_throwsBadRequestException() {
        LoginRequest req = new LoginRequest("ghost@test.com", "Pass@1234");
        // authenticationManager passes but user is gone from DB
        when(userRepository.findByEmail("ghost@test.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.login(req))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("User not found");
    }

    @Test
    void login_disabledUserInDb_throwsBadRequestException() {
        LoginRequest req = new LoginRequest("locked@test.com", "Pass@1234");
        User disabledUser = User.builder()
                .id(5L).email("locked@test.com").firstName("L").lastName("U")
                .passwordHash("h").role(UserRole.ROLE_USER).enabled(false)
                .build();
        when(userRepository.findByEmail("locked@test.com")).thenReturn(Optional.of(disabledUser));

        assertThatThrownBy(() -> authService.login(req))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("disabled");
    }
}
