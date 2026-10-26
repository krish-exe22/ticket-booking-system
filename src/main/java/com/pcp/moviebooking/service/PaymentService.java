package com.pcp.moviebooking.service;

import com.pcp.moviebooking.exception.InvalidBookingException;
import com.pcp.moviebooking.exception.PaymentFailedException;
import com.pcp.moviebooking.model.Payment;
import com.pcp.moviebooking.model.PaymentMethod;
import com.pcp.moviebooking.payment.PaymentGateway;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * Picks the right PaymentGateway implementation (polymorphism) and records the outcome on the Payment.
 */
@Service
public class PaymentService {

    private static final Logger log = LoggerFactory.getLogger(PaymentService.class);

    private final Map<PaymentMethod, PaymentGateway> gateways = new EnumMap<>(PaymentMethod.class);

    // Spring injects every PaymentGateway bean in the application.
    public PaymentService(List<PaymentGateway> gatewayBeans) {
        gatewayBeans.forEach(g -> gateways.put(g.method(), g));
    }

    /**
     * Charges the payment. The checked PaymentFailedException is handled here so that
     * a declined payment becomes a FAILED payment record instead of a crash.
     *
     * @return true if the payment succeeded
     */
    public boolean process(Payment payment, String reference) {
        PaymentGateway gateway = gateways.get(payment.getMethod());
        if (gateway == null) {
            throw new InvalidBookingException("Unsupported payment method: " + payment.getMethod());
        }
        try {
            String transactionId = gateway.charge(payment.getAmount(), reference);
            payment.markSuccess(transactionId);
            log.info("Payment successful, transaction {}", transactionId);
            return true;
        } catch (PaymentFailedException e) {
            payment.markFailed(e.getMessage());
            log.warn("Payment failed: {}", e.getMessage());
            return false;
        } finally {
            log.debug("Payment attempt finished with status {}", payment.getStatus());
        }
    }
}
