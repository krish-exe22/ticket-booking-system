package com.pcp.moviebooking;

import com.pcp.moviebooking.config.DataSeeder;
import com.pcp.moviebooking.dto.Dtos.BookingRequest;
import com.pcp.moviebooking.dto.Dtos.BookingResponse;
import com.pcp.moviebooking.exception.InvalidBookingException;
import com.pcp.moviebooking.exception.SeatUnavailableException;
import com.pcp.moviebooking.model.AppUser;
import com.pcp.moviebooking.model.BookingStatus;
import com.pcp.moviebooking.model.Movie;
import com.pcp.moviebooking.model.PaymentMethod;
import com.pcp.moviebooking.model.PaymentStatus;
import com.pcp.moviebooking.model.SeatStatus;
import com.pcp.moviebooking.model.Show;
import com.pcp.moviebooking.repository.BookingRepository;
import com.pcp.moviebooking.repository.MovieRepository;
import com.pcp.moviebooking.repository.ShowRepository;
import com.pcp.moviebooking.repository.ShowSeatRepository;
import com.pcp.moviebooking.repository.UserRepository;
import com.pcp.moviebooking.service.BookingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@ActiveProfiles("test")
class BookingFlowTest {

    @Autowired BookingService bookingService;
    @Autowired BookingRepository bookingRepository;
    @Autowired ShowSeatRepository seatRepository;
    @Autowired ShowRepository showRepository;
    @Autowired MovieRepository movieRepository;
    @Autowired UserRepository userRepository;

    private Show show;
    private final List<AppUser> users = new ArrayList<>();

    @BeforeEach
    void setUp() {
        bookingRepository.deleteAll();
        seatRepository.deleteAll();
        showRepository.deleteAll();
        movieRepository.deleteAll();
        userRepository.deleteAll();
        users.clear();

        Movie movie = movieRepository.save(new Movie("Test Movie", "Drama", "English", 120, 7.5));
        show = showRepository.save(new Show(movie, "Screen 1",
                LocalDateTime.now().plusDays(1), new BigDecimal("100.00"), 50));
        seatRepository.saveAll(DataSeeder.createSeats(show));
        for (int i = 0; i < 8; i++) {
            users.add(userRepository.save(new AppUser("User" + i, "user" + i + "@test.com")));
        }
    }

    private BookingRequest request(AppUser user, PaymentMethod method, String ref, String... seats) {
        return new BookingRequest(user.getId(), show.getId(), List.of(seats), method, ref);
    }

    @Test
    void successfulBookingMarksSeatsBookedAndPaymentSuccess() {
        BookingResponse r = bookingService.book(request(users.get(0), PaymentMethod.CARD, "4111111111111111", "A1", "A2"));

        assertEquals(BookingStatus.CONFIRMED, r.status());
        assertEquals(PaymentStatus.SUCCESS, r.payment().status());
        assertEquals(0, new BigDecimal("200.00").compareTo(r.totalAmount()));
        assertEquals(2, seatRepository.countByShowIdAndStatus(show.getId(), SeatStatus.BOOKED));
    }

    @Test
    void secondBookingOfSameSeatIsRejected() {
        bookingService.book(request(users.get(0), PaymentMethod.UPI, "a@upi", "B1"));

        assertThrows(SeatUnavailableException.class,
                () -> bookingService.book(request(users.get(1), PaymentMethod.UPI, "b@upi", "B1", "B2")));
        // B2 must not be booked because the whole request was rejected
        assertEquals(1, seatRepository.countByShowIdAndStatus(show.getId(), SeatStatus.BOOKED));
    }

    @Test
    void failedPaymentKeepsSeatsFreeButRecordsHistory() {
        BookingResponse r = bookingService.book(request(users.get(0), PaymentMethod.CARD, "4111111111110000", "C1"));

        assertEquals(BookingStatus.FAILED, r.status());
        assertEquals(PaymentStatus.FAILED, r.payment().status());
        assertEquals(0, seatRepository.countByShowIdAndStatus(show.getId(), SeatStatus.BOOKED));
        assertEquals(1, bookingService.getHistory(users.get(0).getId()).size());
    }

    @Test
    void cancelReleasesSeatsAndRefunds() {
        BookingResponse r = bookingService.book(request(users.get(0), PaymentMethod.UPI, "a@upi", "D1"));
        BookingResponse cancelled = bookingService.cancel(r.id());

        assertEquals(BookingStatus.CANCELLED, cancelled.status());
        assertEquals(PaymentStatus.REFUNDED, cancelled.payment().status());
        assertEquals(0, seatRepository.countByShowIdAndStatus(show.getId(), SeatStatus.BOOKED));
        // the seat can be booked again
        assertEquals(BookingStatus.CONFIRMED,
                bookingService.book(request(users.get(1), PaymentMethod.UPI, "b@upi", "D1")).status());
    }

    @Test
    void tooManySeatsDuplicateSeatsAndUnknownSeatsAreRejected() {
        assertThrows(InvalidBookingException.class, () -> bookingService.book(
                request(users.get(0), PaymentMethod.UPI, "a@upi", "A1", "A2", "A3", "A4", "A5", "A6", "A7")));
        assertThrows(InvalidBookingException.class, () -> bookingService.book(
                request(users.get(0), PaymentMethod.UPI, "a@upi", "A1", "a1")));
        assertThrows(InvalidBookingException.class, () -> bookingService.book(
                request(users.get(0), PaymentMethod.UPI, "a@upi", "Z99")));
    }
}
