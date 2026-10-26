package com.pcp.moviebooking.repository;

import com.pcp.moviebooking.model.SeatStatus;
import com.pcp.moviebooking.model.ShowSeat;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;

public interface ShowSeatRepository extends JpaRepository<ShowSeat, Long> {

    List<ShowSeat> findByShowIdOrderById(Long showId);

    long countByShowIdAndStatus(Long showId, SeatStatus status);

    /**
     * SELECT ... FOR UPDATE. A second transaction asking for the same seats waits
     * until the first one commits, then sees BOOKED and is rejected.
     * Ordering by id means everybody locks seats in the same order (no deadlocks).
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select s from ShowSeat s where s.show.id = :showId and s.seatNumber in :seatNumbers order by s.id")
    List<ShowSeat> findAndLock(@Param("showId") Long showId,
                               @Param("seatNumbers") Collection<String> seatNumbers);
}
