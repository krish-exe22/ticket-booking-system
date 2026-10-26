package com.pcp.moviebooking.config;

import com.pcp.moviebooking.model.AppUser;
import com.pcp.moviebooking.model.Movie;
import com.pcp.moviebooking.model.Show;
import com.pcp.moviebooking.model.ShowSeat;
import com.pcp.moviebooking.repository.MovieRepository;
import com.pcp.moviebooking.repository.ShowRepository;
import com.pcp.moviebooking.repository.ShowSeatRepository;
import com.pcp.moviebooking.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

/** Loads sample movies, shows, seats and one user when the app starts (not in the "test" profile). */
@Configuration
@Profile("!test")
public class DataSeeder {

    private static final Logger log = LoggerFactory.getLogger(DataSeeder.class);
    private static final int ROWS = 5;
    private static final int SEATS_PER_ROW = 10;

    @Bean
    CommandLineRunner seed(MovieRepository movies, ShowRepository shows,
                           ShowSeatRepository seats, UserRepository users) {
        return args -> {
            users.save(new AppUser("Demo User", "demo@example.com"));

            List<Movie> savedMovies = movies.saveAll(List.of(
                    new Movie("Inception", "Sci-Fi", "English", 148, 8.8),
                    new Movie("3 Idiots", "Comedy", "Hindi", 170, 8.4),
                    new Movie("Interstellar", "Sci-Fi", "English", 169, 8.7)));

            LocalDate tomorrow = LocalDate.now().plusDays(1);
            int screenNo = 1;
            for (Movie movie : savedMovies) {
                for (LocalTime time : List.of(LocalTime.of(10, 0), LocalTime.of(14, 30), LocalTime.of(19, 0))) {
                    Show show = shows.save(new Show(movie, "Screen " + screenNo,
                            LocalDateTime.of(tomorrow, time), new BigDecimal("250.00"), ROWS * SEATS_PER_ROW));
                    seats.saveAll(createSeats(show));
                }
                screenNo++;
            }
            log.info("Sample data loaded: {} movies, {} shows", movies.count(), shows.count());
        };
    }

    /** Seat numbers A1..A10, B1..B10, ... */
    public static List<ShowSeat> createSeats(Show show) {
        List<ShowSeat> list = new ArrayList<>();
        for (int r = 0; r < ROWS; r++) {
            char row = (char) ('A' + r);
            for (int n = 1; n <= SEATS_PER_ROW; n++) {
                list.add(new ShowSeat(show, row + String.valueOf(n)));
            }
        }
        return list;
    }
}
