package com.example.backend.Repo;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.backend.Entity.Cinema;
import com.example.backend.Entity.Showdate;
import com.example.backend.Entity.Showtime;

public interface ShowtimeRepo extends JpaRepository<Showtime, Integer> {

    // Find all unique showing dates for a specific cinema
    List<Showtime> findDistinctBycinema(Cinema cinema);

    // Find all showtimes for a specific cinema and date
    List<Showtime> findByCinemaAndShowDate(Cinema cinema, Showdate showDateId);
}
