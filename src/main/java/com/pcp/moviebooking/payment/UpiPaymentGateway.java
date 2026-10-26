package com.pcp.moviebooking.payment;

import com.pcp.moviebooking.exception.PaymentFailedException;
import com.pcp.moviebooking.model.PaymentMethod;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.UUID;

/** Simulated UPI gateway. A UPI id must contain '@'; ids starting with "fail" are declined. */
@Component
public class UpiPaymentGateway implements PaymentGateway {

    @Override
    public PaymentMethod method() {
        return PaymentMethod.UPI;
    }

    @Override
    public String charge(BigDecimal amount, String reference) throws PaymentFailedException {
        if (reference == null || !reference.contains("@")) {
            throw new PaymentFailedException("Invalid UPI id");
        }
        if (reference.toLowerCase().startsWith("fail")) {
            throw new PaymentFailedException("UPI payment rejected");
        }
        return "UPI-" + UUID.randomUUID();
    }
}
