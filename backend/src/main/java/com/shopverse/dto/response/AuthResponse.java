package com.shopverse.dto.response;
import lombok.*;
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class AuthResponse {
    private String token;
    private String type = "Bearer";
    private String email;
    private String firstName;
    private String lastName;
    private String role;
}
