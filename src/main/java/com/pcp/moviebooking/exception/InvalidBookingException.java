package com.pcp.moviebooking.exception;

/** Unchecked exception: the booking request breaks a business rule. */
public class InvalidBookingException extends RuntimeException {

    public InvalidBookingException(String message) {
        super(message);
    }
}
