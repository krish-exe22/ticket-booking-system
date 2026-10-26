package com.pcp.moviebooking.exception;

import java.util.List;

/** Unchecked exception: at least one requested seat is already booked (double-booking attempt). */
public class SeatUnavailableException extends RuntimeException {

    private final List<String> seats;

    public SeatUnavailableException(List<String> seats) {
        super("Seat(s) not available: " + String.join(", ", seats));
        this.seats = seats;
    }

    public List<String> getSeats() {
        return seats;
    }
}
