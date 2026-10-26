package com.pcp.moviebooking;

import com.pcp.moviebooking.config.DataSeeder;
import com.pcp.moviebooking.dto.Dtos.BookingRequest;
import com.pcp.moviebooking.model.AppUser;
import com.pcp.moviebooking.model.BookingStatus;
import com.pcp.moviebooking.model.Movie;
import com.pcp.moviebooking.model.PaymentMethod;
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
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;

/** Several threads (Thread + Runnable) fight for the same seat: exactly one must win. */
@SpringBootTest
@ActiveProfiles("test")
@DirtiesContext
class ConcurrencyTest {

    @Autowired BookingService bookingService;
    @Autowired BookingRepository bookingRepository;
    @Autowired ShowSeatRepository seatRepository;
    @Autowired ShowRepository showRepository;
    @Autowired MovieRepository movieRepository;
    @Autowired UserRepository userRepository;

    @BeforeEach
    void setUp() {
        bookingRepository.deleteAll();
        seatRepository.deleteAll();
        showRepository.deleteAll();
        movieRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    void onlyOneThreadWinsTheSameSeat() throws InterruptedException {
        Movie movie = movieRepository.save(new Movie("Race", "Thriller", "English", 100, 7.0));
        Show show = showRepository.save(new Show(movie, "Screen 9",
                LocalDateTime.now().plusDays(1), new BigDecimal("100.00"), 50));
        seatRepository.saveAll(DataSeeder.createSeats(show));

        List<AppUser> users = new ArrayList<>();
        for (int i = 0; i < 8; i++) {
            users.add(userRepository.save(new AppUser("Racer" + i, "racer" + i + "@test.com")));
        }

        AtomicInteger success = new AtomicInteger();
        AtomicInteger rejected = new AtomicInteger();
        CountDownLatch startSignal = new CountDownLatch(1);
        List<Thread> threads = new ArrayList<>();

        for (AppUser user : users) {
            Runnable task = () -> {
                try {
                    startSignal.await();   // all threads start together
                    bookingService.book(new BookingRequest(user.getId(), show.getId(),
                            List.of("E5"), PaymentMethod.UPI, "x@upi"));
                    success.incrementAndGet();
                } catch (Exception e) {
                    rejected.incrementAndGet();
                }
            };
            Thread t = new Thread(task, "booking-" + user.getName());
            threads.add(t);
            t.start();
        }
        startSignal.countDown();
        for (Thread t : threads) {
            t.join();
        }

        assertEquals(1, success.get(), "exactly one booking must succeed");
        assertEquals(users.size() - 1, rejected.get());
        assertEquals(1, seatRepository.countByShowIdAndStatus(show.getId(), SeatStatus.BOOKED));
        assertEquals(1, bookingRepository.findAll().stream()
                .filter(b -> b.getStatus() == BookingStatus.CONFIRMED).count());
    }
}
