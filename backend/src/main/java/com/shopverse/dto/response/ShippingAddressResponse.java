package com.shopverse.dto.response;
import lombok.*;
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class ShippingAddressResponse {
    private String firstName;
    private String lastName;
    private String street;
    private String city;
    private String state;
    private String zipCode;
    private String country;
}
