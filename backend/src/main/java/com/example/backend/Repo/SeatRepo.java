package com.example.backend.Repo;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.backend.Entity.Cinema;
import com.example.backend.Entity.Hall;
import com.example.backend.Entity.Seat;
import com.example.backend.Entity.Showtime;

public interface SeatRepo extends JpaRepository<Seat, Long> {

    // Find a specific seat
    Seat findByShowtimeIdAndCinemaIdAndHallIdAndSeatNumber(Showtime showtimeId, Cinema cinemaId, Hall hallId, String seatNumber);

    // Used by PaymentService once a booking is confirmed paid, to clear the
    // seat's reservation timestamp so it's never swept by the cleanup task.
    @Query("SELECT s FROM Seat s WHERE s.showtimeId.showtime_id = :showtimeId AND s.seatNumber = :seatNumber")
    Optional<Seat> findByShowtimeIdAndSeatNumber(@Param("showtimeId") Integer showtimeId, @Param("seatNumber") String seatNumber);

    // Used by SeatReservationCleanupTask: any seat still held (reservedAt is
    // set — a permanently booked seat has it cleared to null) past the TTL
    // gets released back to available.
    List<Seat> findBySeatAvailabilityFalseAndReservedAtBefore(LocalDateTime cutoff);

    // Define an auto-generated query using naming conventions
    @Query("SELECT s FROM Seat s WHERE s.showtimeId.cinema.cinema_id = :cinemaId AND s.showtimeId.hall.hall_id = :hallId AND s.showtimeId.showDate.show_date = :showDate")
    List<Seat> findSeatsByCinemaIdAndHallIdAndShowDate(@Param("cinemaId") Integer cinemaId, @Param("hallId") Integer hallId, @Param("showDate") String showDate);

    @Query("SELECT s FROM Seat s WHERE s.showtimeId.showtime_id = :showtimeId AND s.cinemaId.cinema_id = :cinemaId AND s.hallId.hall_id = :hallId AND s.showtimeId.showDate.show_date = :showDate")
    List<Seat> findByShowtimeCinemaHallAndDate(
        @Param("showtimeId") Integer showtimeId,
        @Param("cinemaId") Integer cinemaId,
        @Param("hallId") Integer hallId,
        @Param("showDate") String showDate);



}
