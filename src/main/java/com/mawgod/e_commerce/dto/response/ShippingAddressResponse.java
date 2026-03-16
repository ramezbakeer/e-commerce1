package com.mawgod.e_commerce.dto.response;

public record ShippingAddressResponse(
        String street,
        String city,
        String state,
        String postalCode,
        String country
) {}
