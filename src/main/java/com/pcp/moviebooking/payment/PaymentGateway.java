package com.pcp.moviebooking.payment;

import com.pcp.moviebooking.exception.PaymentFailedException;
import com.pcp.moviebooking.model.PaymentMethod;

import java.math.BigDecimal;

/**
 * Interface: every payment method implements it differently (polymorphism).
 * Declared as a functional-style contract: one charge operation.
 */
public interface PaymentGateway {

    PaymentMethod method();

    /**
     * @param reference card number / UPI id supplied by the customer (never stored)
     * @return transaction id
     * @throws PaymentFailedException when the payment is declined
     */
    String charge(BigDecimal amount, String reference) throws PaymentFailedException;
}
