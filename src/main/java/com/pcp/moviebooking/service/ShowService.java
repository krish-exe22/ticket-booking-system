package com.pcp.moviebooking.service;

import com.pcp.moviebooking.dto.Dtos.SeatResponse;
import com.pcp.moviebooking.dto.Dtos.ShowResponse;
import com.pcp.moviebooking.exception.ResourceNotFoundException;
import com.pcp.moviebooking.model.SeatStatus;
import com.pcp.moviebooking.model.Show;
import com.pcp.moviebooking.repository.MovieRepository;
import com.pcp.moviebooking.repository.ShowRepository;
import com.pcp.moviebooking.repository.ShowSeatRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class ShowService {

    private final ShowRepository showRepository;
    private final ShowSeatRepository showSeatRepository;
    private final MovieRepository movieRepository;

    public ShowService(ShowRepository showRepository,
                       ShowSeatRepository showSeatRepository,
                       MovieRepository movieRepository) {
        this.showRepository = showRepository;
        this.showSeatRepository = showSeatRepository;
        this.movieRepository = movieRepository;
    }

    public List<ShowResponse> getShowsForMovie(Long movieId) {
        if (!movieRepository.existsById(movieId)) {
            throw new ResourceNotFoundException("Movie", movieId);
        }
        return showRepository.findByMovieIdOrderByStartTime(movieId).stream()
                .map(this::toResponse)
                .toList();
    }

    public ShowResponse getShow(Long showId) {
        return toResponse(findShow(showId));
    }

    /** Seat map of a show with the current availability of every seat. */
    public List<SeatResponse> getSeats(Long showId) {
        findShow(showId);
        return showSeatRepository.findByShowIdOrderById(showId).stream()
                .map(s -> new SeatResponse(s.getSeatNumber(), s.getStatus()))
                .toList();
    }

    private Show findShow(Long showId) {
        return showRepository.findById(showId)
                .orElseThrow(() -> new ResourceNotFoundException("Show", showId));
    }

    private ShowResponse toResponse(Show s) {
        long available = showSeatRepository.countByShowIdAndStatus(s.getId(), SeatStatus.AVAILABLE);
        return new ShowResponse(s.getId(), s.getMovie().getId(), s.getMovie().getTitle(), s.getScreen(),
                s.getStartTime(), s.getPrice(), s.getTotalSeats(), available);
    }
}
