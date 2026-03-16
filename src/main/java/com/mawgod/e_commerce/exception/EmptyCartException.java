package com.mawgod.e_commerce.exception;

public class EmptyCartException extends RuntimeException {

    public EmptyCartException() {
        super("Cannot checkout: cart is empty");
    }
}
