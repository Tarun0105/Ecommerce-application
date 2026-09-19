package com.shopverse.dto.request;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.*;
@Data @NoArgsConstructor @AllArgsConstructor
public class CheckoutRequest {
    @NotNull(message = "Shipping address is required")
    @Valid
    private ShippingAddressRequest shippingAddress;
    private String notes;
}
