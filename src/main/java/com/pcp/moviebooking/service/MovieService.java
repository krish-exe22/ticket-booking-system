package com.pcp.moviebooking.service;

import com.pcp.moviebooking.dto.Dtos.MovieResponse;
import com.pcp.moviebooking.exception.ResourceNotFoundException;
import com.pcp.moviebooking.model.Movie;
import com.pcp.moviebooking.repository.MovieRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class MovieService {

    private final MovieRepository movieRepository;

    // Constructor injection (Dependency Injection)
    public MovieService(MovieRepository movieRepository) {
        this.movieRepository = movieRepository;
    }

    public List<MovieResponse> getAllMovies() {
        return movieRepository.findAll().stream()   // Stream API
                .map(this::toResponse)               // method reference
                .toList();
    }

    public MovieResponse getMovie(Long id) {
        return movieRepository.findById(id)
                .map(this::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Movie", id));  // lambda
    }

    private MovieResponse toResponse(Movie m) {
        return new MovieResponse(m.getId(), m.getTitle(), m.getGenre(), m.getLanguage(),
                m.getDurationMinutes(), m.getRating());
    }
}
