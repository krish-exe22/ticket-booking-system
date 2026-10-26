package com.pcp.moviebooking.payment;

import com.pcp.moviebooking.exception.PaymentFailedException;
import com.pcp.moviebooking.model.PaymentMethod;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.UUID;

/** Simulated card gateway. A card number ending in "0000" is declined. */
@Component
public class CardPaymentGateway implements PaymentGateway {

    @Override
    public PaymentMethod method() {
        return PaymentMethod.CARD;
    }

    @Override
    public String charge(BigDecimal amount, String reference) throws PaymentFailedException {
        if (reference == null || reference.length() < 4) {
            throw new PaymentFailedException("Invalid card number");
        }
        if (reference.endsWith("0000")) {
            throw new PaymentFailedException("Card declined by bank");
        }
        return "CARD-" + UUID.randomUUID();
    }
}
