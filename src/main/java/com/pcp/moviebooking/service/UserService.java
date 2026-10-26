package com.pcp.moviebooking.service;

import com.pcp.moviebooking.dto.Dtos.UserRequest;
import com.pcp.moviebooking.dto.Dtos.UserResponse;
import com.pcp.moviebooking.exception.InvalidBookingException;
import com.pcp.moviebooking.exception.ResourceNotFoundException;
import com.pcp.moviebooking.model.AppUser;
import com.pcp.moviebooking.model.BookingStatus;
import com.pcp.moviebooking.repository.BookingRepository;
import com.pcp.moviebooking.repository.UserRepository;
import com.pcp.moviebooking.dto.Dtos.BookingResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final BookingRepository bookingRepository;

    public UserService(UserRepository userRepository, BookingRepository bookingRepository) {
        this.userRepository = userRepository;
        this.bookingRepository = bookingRepository;
    }

    @Transactional
    public UserResponse register(UserRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new InvalidBookingException("Email already registered: " + request.email());
        }
        AppUser saved = userRepository.save(new AppUser(request.name(), request.email()));
        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public UserResponse getUser(Long id) {
        return userRepository.findById(id)
                .map(this::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("User", id));
    }

    @Transactional(readOnly = true)
    public List<BookingResponse> getHistory(Long id) {
        if (!userRepository.existsById(id)) {
            throw new ResourceNotFoundException("User", id);
        }
        return bookingRepository.findByUserIdOrderByIdDesc(id).stream()
                .map(this::toBookingResponse)
                .toList();
    }

    private UserResponse toResponse(AppUser u) {
        return new UserResponse(u.getId(), u.getName(), u.getEmail());
    }

    private BookingResponse toBookingResponse(com.pcp.moviebooking.model.Booking b) {
        // Minimal mapping - in a real app use MapStruct
        com.pcp.moviebooking.dto.Dtos.PaymentResponse payment = new com.pcp.moviebooking.dto.Dtos.PaymentResponse(
                b.getPayment().getMethod(),
                b.getPayment().getStatus(),
                b.getPayment().getAmount(),
                b.getPayment().getTransactionId(),
                b.getPayment().getFailureReason(),
                b.getPayment().getPaidAt()
        );
        return new BookingResponse(
                b.getId(),
                b.getUser().getId(),
                b.getShow().getMovie().getTitle(),
                b.getShow().getId(),
                b.getShow().getStartTime(),
                b.getShow().getScreen(),
                List.copyOf(b.getSeatNumbers()),
                b.getTotalAmount(),
                b.getStatus(),
                payment,
                b.getCreatedAt()
        );
    }
}
