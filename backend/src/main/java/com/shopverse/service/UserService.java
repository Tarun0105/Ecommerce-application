package com.shopverse.service;

import com.shopverse.dto.request.*;
import com.shopverse.dto.response.*;
import com.shopverse.entity.*;
import com.shopverse.exception.*;
import com.shopverse.mapper.UserMapper;
import com.shopverse.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;

    public UserResponse getCurrentUser(String email) {
        return userMapper.toResponse(userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + email)));
    }

    @Transactional
    public UserResponse updateProfile(String email, UpdateProfileRequest request) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + email));
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        return userMapper.toResponse(userRepository.save(user));
    }

    @Transactional
    public void changePassword(String email, ChangePasswordRequest request) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + email));
        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPasswordHash())) {
            throw new BadRequestException("Current password is incorrect.");
        }
        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
    }

    @Transactional(readOnly = true)
    public PageResponse<UserResponse> getAllUsers(int page, int size, String search) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<User> users;
        if (StringUtils.hasText(search)) {
            users = userRepository.findByFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCaseOrEmailContainingIgnoreCase(
                    search, search, search, pageable);
        } else {
            users = userRepository.findAll(pageable);
        }
        return PageResponse.from(userMapper.toResponsePage(users));
    }

    public UserResponse getUserById(Long id) {
        return userMapper.toResponse(userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", id)));
    }

    @Transactional
    public UserResponse toggleUserStatus(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", id));
        if (user.getRole() == UserRole.ROLE_ADMIN) {
            throw new BadRequestException("Cannot disable an admin account.");
        }
        user.setEnabled(!user.isEnabled());
        return userMapper.toResponse(userRepository.save(user));
    }
}
