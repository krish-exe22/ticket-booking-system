package com.pcp.moviebooking.service;

import com.pcp.moviebooking.dto.Dtos.BookingResponse;
import com.pcp.moviebooking.dto.Dtos.PaymentResponse;
import com.pcp.moviebooking.model.Booking;
import com.pcp.moviebooking.model.Payment;
import org.springframework.stereotype.Component;

import java.util.List;

/** Converts Booking entities to API responses. */
@Component
public class BookingMapper {

    public BookingResponse toResponse(Booking b) {
        Payment p = b.getPayment();
        PaymentResponse payment = new PaymentResponse(p.getMethod(), p.getStatus(), p.getAmount(),
                p.getTransactionId(), p.getFailureReason(), p.getPaidAt());
        return new BookingResponse(b.getId(), b.getUser().getId(), b.getShow().getMovie().getTitle(),
                b.getShow().getId(), b.getShow().getStartTime(), b.getShow().getScreen(),
                List.copyOf(b.getSeatNumbers()), b.getTotalAmount(), b.getStatus(), payment, b.getCreatedAt());
    }
}
