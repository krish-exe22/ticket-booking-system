package com.pcp.moviebooking.exception;

/**
 * CHECKED exception: a payment gateway declares it with "throws" and the caller
 * is forced to handle it with try/catch.
 */
public class PaymentFailedException extends Exception {

    public PaymentFailedException(String message) {
        super(message);
    }
}
