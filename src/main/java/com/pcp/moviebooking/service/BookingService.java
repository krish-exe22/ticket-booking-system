package com.pcp.moviebooking.service;

import com.pcp.moviebooking.dto.Dtos.BookingRequest;
import com.pcp.moviebooking.dto.Dtos.BookingResponse;
import com.pcp.moviebooking.exception.InvalidBookingException;
import com.pcp.moviebooking.exception.ResourceNotFoundException;
import com.pcp.moviebooking.exception.SeatUnavailableException;
import com.pcp.moviebooking.model.AppUser;
import com.pcp.moviebooking.model.Booking;
import com.pcp.moviebooking.model.BookingStatus;
import com.pcp.moviebooking.model.Payment;
import com.pcp.moviebooking.model.PaymentStatus;
import com.pcp.moviebooking.model.Show;
import com.pcp.moviebooking.model.ShowSeat;
import com.pcp.moviebooking.repository.BookingRepository;
import com.pcp.moviebooking.repository.ShowRepository;
import com.pcp.moviebooking.repository.ShowSeatRepository;
import com.pcp.moviebooking.repository.UserRepository;
import com.pcp.moviebooking.service.BookingMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class BookingService {

    private static final Logger log = LoggerFactory.getLogger(BookingService.class);

    private final BookingRepository bookingRepository;
    private final ShowRepository showRepository;
    private final ShowSeatRepository showSeatRepository;
    private final UserRepository userRepository;
    private final PaymentService paymentService;
    private final BookingMapper mapper;
    private final int maxSeatsPerBooking;

    /** Static lock serializing all seat-booking operations across concurrent threads. */
    private static final Object GLOBAL_BOOKING_LOCK = new Object();

    public BookingService(BookingRepository bookingRepository,
                          ShowRepository showRepository,
                          ShowSeatRepository showSeatRepository,
                          UserRepository userRepository,
                          PaymentService paymentService,
                          BookingMapper mapper,
                          @Value("${booking.max-seats-per-booking}") int maxSeatsPerBooking) {
        this.bookingRepository = bookingRepository;
        this.showRepository = showRepository;
        this.showSeatRepository = showSeatRepository;
        this.userRepository = userRepository;
        this.paymentService = paymentService;
        this.mapper = mapper;
        this.maxSeatsPerBooking = maxSeatsPerBooking;
    }

    @Transactional
    public BookingResponse book(BookingRequest request) {
        List<String> seatNumbers = validateSeats(request.seatNumbers());

        AppUser user = userRepository.findById(request.userId())
                .orElseThrow(() -> new ResourceNotFoundException("User", request.userId()));
        Show show = showRepository.findById(request.showId())
                .orElseThrow(() -> new ResourceNotFoundException("Show", request.showId()));

        if (show.getStartTime().isBefore(LocalDateTime.now())) {
            throw new InvalidBookingException("This show has already started");
        }

        // Serialize seat-locking per show so concurrent threads can't both claim the same seat
        synchronized (GLOBAL_BOOKING_LOCK) {
            List<ShowSeat> seats = showSeatRepository.findAndLock(show.getId(), seatNumbers);
            checkSeats(seatNumbers, seats);

            BigDecimal total = show.getPrice().multiply(BigDecimal.valueOf(seatNumbers.size()));
            Payment payment = new Payment(total, request.paymentMethod());

            Booking booking = new Booking(user, show, seatNumbers, total, payment);

            if (paymentService.process(payment, request.paymentReference())) {
                seats.forEach(ShowSeat::book);
                booking.confirm();
                log.info("Booking confirmed for user {} show {} seats {}", user.getId(), show.getId(), seatNumbers);
            } else {
                booking.fail();
            }
            return mapper.toResponse(bookingRepository.save(booking));
        }
    }

    @Transactional
    public BookingResponse cancel(Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking", bookingId));

        if (booking.getStatus() != BookingStatus.CONFIRMED) {
            throw new InvalidBookingException("Only confirmed bookings can be cancelled (status is "
                    + booking.getStatus() + ")");
        }
        if (booking.getShow().getStartTime().isBefore(LocalDateTime.now())) {
            throw new InvalidBookingException("The show has already started");
        }

        showSeatRepository.findAndLock(booking.getShow().getId(), booking.getSeatNumbers())
                .forEach(ShowSeat::release);
        booking.cancel();
        if (booking.getPayment().getStatus() == PaymentStatus.SUCCESS) {
            booking.getPayment().markRefunded();
        }
        log.info("Booking {} cancelled", bookingId);
        return mapper.toResponse(booking);
    }

    @Transactional(readOnly = true)
    public BookingResponse getBooking(Long bookingId) {
        return bookingRepository.findById(bookingId)
                .map(mapper::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Booking", bookingId));
    }

    @Transactional(readOnly = true)
    public List<BookingResponse> getHistory(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new ResourceNotFoundException("User", userId);
        }
        return bookingRepository.findByUserIdOrderByIdDesc(userId).stream()
                .map(mapper::toResponse)
                .toList();
    }

    private void checkSeats(List<String> requested, List<ShowSeat> seats) {
        if (seats.size() != requested.size()) {
            Set<String> found = seats.stream().map(ShowSeat::getSeatNumber).collect(Collectors.toSet());
            List<String> unknown = requested.stream().filter(n -> !found.contains(n)).toList();
            throw new InvalidBookingException("Unknown seat(s) for this show: " + String.join(", ", unknown));
        }
        List<String> taken = seats.stream()
                .filter(s -> !s.isAvailable())
                .map(ShowSeat::getSeatNumber)
                .toList();
        if (!taken.isEmpty()) {
            throw new SeatUnavailableException(taken);
        }
    }

    private List<String> validateSeats(List<String> requested) {
        List<String> seats = requested.stream()
                .map(String::trim)
                .map(String::toUpperCase)
                .toList();
        Set<String> unique = new HashSet<>(seats);
        if (unique.size() != seats.size()) {
            throw new InvalidBookingException("Duplicate seats in request");
        }
        if (seats.size() > maxSeatsPerBooking) {
            throw new InvalidBookingException("You can book at most " + maxSeatsPerBooking + " seats at a time");
        }
        return seats;
    }
}