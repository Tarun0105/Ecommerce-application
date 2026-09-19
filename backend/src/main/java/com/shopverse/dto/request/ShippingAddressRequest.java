package com.shopverse.dto.request;
import jakarta.validation.constraints.*;
import lombok.*;
@Data @NoArgsConstructor @AllArgsConstructor
public class ShippingAddressRequest {
    @NotBlank(message = "First name is required") private String firstName;
    @NotBlank(message = "Last name is required") private String lastName;
    @NotBlank(message = "Street address is required") private String street;
    @NotBlank(message = "City is required") private String city;
    @NotBlank(message = "State is required") private String state;
    @NotBlank(message = "ZIP code is required") private String zipCode;
    @NotBlank(message = "Country is required") private String country;
}
