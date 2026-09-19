package com.shopverse.entity;

import jakarta.persistence.*;
import lombok.*;

@Embeddable
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ShippingAddress {
    @Column(name = "shipping_first_name", nullable = false, length = 100)
    private String firstName;

    @Column(name = "shipping_last_name", nullable = false, length = 100)
    private String lastName;

    @Column(name = "shipping_street", nullable = false, length = 255)
    private String street;

    @Column(name = "shipping_city", nullable = false, length = 100)
    private String city;

    @Column(name = "shipping_state", nullable = false, length = 100)
    private String state;

    @Column(name = "shipping_zip_code", nullable = false, length = 20)
    private String zipCode;

    @Column(name = "shipping_country", nullable = false, length = 100)
    private String country;
}
