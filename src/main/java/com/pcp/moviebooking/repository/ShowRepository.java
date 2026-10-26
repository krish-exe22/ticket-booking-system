package com.pcp.moviebooking.repository;

import com.pcp.moviebooking.model.Show;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ShowRepository extends JpaRepository<Show, Long> {

    List<Show> findByMovieIdOrderByStartTime(Long movieId);
}
