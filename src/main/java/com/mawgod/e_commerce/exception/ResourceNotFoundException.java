package com.mawgod.e_commerce.exception;

public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String resourceName, String field, Object value) {
        super(String.format("%s not found with %s: '%s'", resourceName, field, value));
    }

    public ResourceNotFoundException(String message) {
        super(message);
    }
}
