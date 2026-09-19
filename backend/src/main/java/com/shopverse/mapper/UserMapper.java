package com.shopverse.mapper;
import com.shopverse.dto.response.*;
import com.shopverse.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {
    public UserResponse toResponse(User u) {
        if (u == null) return null;
        return UserResponse.builder()
                .id(u.getId()).firstName(u.getFirstName()).lastName(u.getLastName())
                .email(u.getEmail()).role(u.getRole().name())
                .enabled(u.isEnabled()).createdAt(u.getCreatedAt())
                .build();
    }
    public Page<UserResponse> toResponsePage(Page<User> page) {
        return page.map(this::toResponse);
    }
}
