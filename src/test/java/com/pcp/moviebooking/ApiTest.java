package com.pcp.moviebooking;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Runs with the seeded sample data (default profile): tests the whole REST flow end to end. */
@SpringBootTest
@AutoConfigureMockMvc
class ApiTest {

    @Autowired MockMvc mvc;

    @Test
    void listsMoviesShowsAndSeats() throws Exception {
        mvc.perform(get("/api/movies"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(3));
        mvc.perform(get("/api/movies/1/shows"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].availableSeats").value(50));
        mvc.perform(get("/api/shows/1/seats"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(50));
    }

    @Test
    void unknownMovieReturns404() throws Exception {
        mvc.perform(get("/api/movies/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Movie not found with id 999"));
    }

    @Test
    void bookingTwiceReturns409AndHistoryIsKept() throws Exception {
        String body = """
                {"userId":1,"showId":2,"seatNumbers":["A1"],"paymentMethod":"UPI","paymentReference":"me@upi"}""";
        mvc.perform(post("/api/bookings").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("CONFIRMED"))
                .andExpect(jsonPath("$.payment.status").value("SUCCESS"));
        mvc.perform(post("/api/bookings").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isConflict());
        mvc.perform(get("/api/users/1/bookings"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].seats[0]").value("A1"));
    }

    @Test
    void invalidRequestReturns400() throws Exception {
        mvc.perform(post("/api/bookings").contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isBadRequest());
    }
}
