package com.pcp.moviebooking.controller;

import com.pcp.moviebooking.dto.Dtos.UserRequest;
import com.pcp.moviebooking.dto.Dtos.UserResponse;
import com.pcp.moviebooking.dto.Dtos.BookingResponse;
import com.pcp.moviebooking.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UserResponse register(@Valid @RequestBody UserRequest request) {
        return userService.register(request);
    }

    @GetMapping("/{id}")
    public UserResponse get(@PathVariable Long id) {
        return userService.getUser(id);
    }

    /** Booking history (with payment status) of a user. */
    @GetMapping("/{id}/bookings")
    public List<BookingResponse> history(@PathVariable Long id) {
        return userService.getHistory(id);
    }
}
