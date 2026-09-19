package com.shopverse.dto.request;
import jakarta.validation.constraints.*;
import lombok.*;
@Data @NoArgsConstructor @AllArgsConstructor
public class UpdateProfileRequest {
    @NotBlank(message = "First name is required") @Size(max=100) private String firstName;
    @NotBlank(message = "Last name is required") @Size(max=100) private String lastName;
}
