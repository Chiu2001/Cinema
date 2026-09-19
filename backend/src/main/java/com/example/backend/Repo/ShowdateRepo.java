package com.example.backend.Repo;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.backend.Entity.Showdate;

public interface ShowdateRepo extends JpaRepository<Showdate, Integer> {

    // Used when adding a showtime: reuse the Showdate row for a date if one
    // already exists, instead of creating a duplicate for the same day.
    //
    // Explicit @Query because the entity's field is literally named
    // "show_date" (with an underscore); Spring Data's method-name parser
    // treats the underscore in "findByShow_date" as a manual nested-property
    // separator (like findByAddress_City), so it tries to resolve a "show"
    // property and fails. An explicit query sidesteps that parsing entirely.
    @Query("SELECT s FROM Showdate s WHERE s.show_date = :show_date")
    Optional<Showdate> findByShow_date(@Param("show_date") String show_date);
}
