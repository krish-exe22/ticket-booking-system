package com.pcp.moviebooking.controller;

import com.pcp.moviebooking.dto.Dtos.SeatResponse;
import com.pcp.moviebooking.dto.Dtos.ShowResponse;
import com.pcp.moviebooking.service.ShowService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/shows")
public class ShowController {

    private final ShowService showService;

    public ShowController(ShowService showService) {
        this.showService = showService;
    }

    @GetMapping("/{id}")
    public ShowResponse get(@PathVariable Long id) {
        return showService.getShow(id);
    }

    /** Seat map with availability. */
    @GetMapping("/{id}/seats")
    public List<SeatResponse> seats(@PathVariable Long id) {
        return showService.getSeats(id);
    }
}
