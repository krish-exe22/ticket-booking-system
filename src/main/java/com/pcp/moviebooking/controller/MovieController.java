package com.pcp.moviebooking.controller;

import com.pcp.moviebooking.dto.Dtos.MovieResponse;
import com.pcp.moviebooking.dto.Dtos.ShowResponse;
import com.pcp.moviebooking.service.MovieService;
import com.pcp.moviebooking.service.ShowService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/movies")
public class MovieController {

    private final MovieService movieService;
    private final ShowService showService;

    public MovieController(MovieService movieService, ShowService showService) {
        this.movieService = movieService;
        this.showService = showService;
    }

    @GetMapping
    public List<MovieResponse> list() {
        return movieService.getAllMovies();
    }

    @GetMapping("/{id}")
    public MovieResponse get(@PathVariable Long id) {
        return movieService.getMovie(id);
    }

    /** Show timings of a movie. */
    @GetMapping("/{id}/shows")
    public List<ShowResponse> shows(@PathVariable Long id) {
        return showService.getShowsForMovie(id);
    }
}
