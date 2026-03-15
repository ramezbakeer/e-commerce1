package com.mawgod.e_commerce.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ShippingAddress {

    @NotBlank
    @Size(max = 255)
    @Column(name = "shipping_street")
    private String street;

    @Size(max = 100)
    @Column(name = "shipping_city", length = 100)
    private String city;

    @Size(max = 100)
    @Column(name = "shipping_state", length = 100)
    private String state;

    @Size(max = 20)
    @Column(name = "shipping_postal_code", length = 20)
    private String postalCode;

    @Size(max = 100)
    @Column(name = "shipping_country", length = 100)
    private String country;
}
