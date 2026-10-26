package com.pcp.moviebooking.dto;

import com.pcp.moviebooking.model.BookingStatus;
import com.pcp.moviebooking.model.PaymentMethod;
import com.pcp.moviebooking.model.PaymentStatus;
import com.pcp.moviebooking.model.SeatStatus;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/** Request/response objects of the REST API, grouped in one file as Java records. */
public final class Dtos {

    private Dtos() {
    }

    public record MovieResponse(Long id, String title, String genre, String language,
                                int durationMinutes, double rating) {
    }

    public record ShowResponse(Long id, Long movieId, String movieTitle, String screen,
                               LocalDateTime startTime, BigDecimal price,
                               int totalSeats, long availableSeats) {
    }

    public record SeatResponse(String seatNumber, SeatStatus status) {
    }

    public record UserRequest(@NotBlank String name, @NotBlank @Email String email) {
    }

    public record UserResponse(Long id, String name, String email) {
    }

    public record BookingRequest(@NotNull Long userId,
                                 @NotNull Long showId,
                                 @NotEmpty List<String> seatNumbers,
                                 @NotNull PaymentMethod paymentMethod,
                                 @NotBlank String paymentReference) {
    }

    public record PaymentResponse(PaymentMethod method, PaymentStatus status, BigDecimal amount,
                                  String transactionId, String failureReason, LocalDateTime paidAt) {
    }

    public record BookingResponse(Long id, Long userId, String movieTitle, Long showId,
                                  LocalDateTime showTime, String screen, List<String> seats,
                                  BigDecimal totalAmount, BookingStatus status,
                                  PaymentResponse payment, LocalDateTime bookedAt) {
    }
}
