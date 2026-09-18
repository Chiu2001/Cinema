package com.example.backend.Repo;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.backend.Entity.Showdate;

public interface ShowdateRepo extends JpaRepository<Showdate, Integer> {

    // Used when adding a showtime: reuse the Showdate row for a date if one
    // already exists, instead of creating a duplicate for the same day.
    Optional<Showdate> findByShow_date(String show_date);
}
