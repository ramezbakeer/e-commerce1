package com.mawgod.e_commerce.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CheckoutRequest(

        @NotBlank(message = "Street address is required")
        @Size(max = 255)
        String street,

        @Size(max = 100)
        String city,

        @Size(max = 100)
        String state,

        @Size(max = 20)
        String postalCode,

        @Size(max = 100)
        String country
) {}
