package com.pcp.moviebooking.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "movies")
public class Movie extends BaseEntity {

    private String title;
    private String genre;
    private String language;
    private int durationMinutes;
    private double rating;

    protected Movie() {
        // required by JPA
    }

    public Movie(String title, String genre, String language, int durationMinutes, double rating) {
        this.title = title;
        this.genre = genre;
        this.language = language;
        this.durationMinutes = durationMinutes;
        this.rating = rating;
    }

    public String getTitle() {
        return title;
    }

    public String getGenre() {
        return genre;
    }

    public String getLanguage() {
        return language;
    }

    public int getDurationMinutes() {
        return durationMinutes;
    }

    public double getRating() {
        return rating;
    }
}
