package com.pcp.moviebooking.model;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * A screening of a movie on a screen at a given time.
 * (Table is called movie_shows because SHOW is a reserved word in some databases.)
 */
@Entity
@Table(name = "movie_shows")
public class Show extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "movie_id")
    private Movie movie;

    private String screen;
    private LocalDateTime startTime;
    private BigDecimal price;
    private int totalSeats;

    protected Show() {
        // required by JPA
    }

    public Show(Movie movie, String screen, LocalDateTime startTime, BigDecimal price, int totalSeats) {
        this.movie = movie;
        this.screen = screen;
        this.startTime = startTime;
        this.price = price;
        this.totalSeats = totalSeats;
    }

    public Movie getMovie() {
        return movie;
    }

    public String getScreen() {
        return screen;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public int getTotalSeats() {
        return totalSeats;
    }
}
