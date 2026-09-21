package com.example.backend.Repo;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import com.example.backend.Entity.Showdate;

// Not a unit test — a query like this can only be proven correct by
// actually running it against a real JPA provider, since the thing being
// tested IS the @Query itself. @DataJpaTest loads just the JPA layer
// against an in-memory H2 database (nothing is mocked, no real MariaDB
// needed), then rolls the transaction back after each test.
//
// This is the regression test for the bug that crashed the app on startup:
// Spring Data's method-name parser read the underscore in "findByShow_date"
// as a manual nested-property separator instead of a literal character in
// the field name, and threw before the app ever finished booting.
@DataJpaTest
class ShowdateRepoTest {

    @Autowired
    private ShowdateRepo showdateRepo;

    @Test
    void findByShow_date_matchingRowExists_returnsIt() {
        Showdate showdate = new Showdate();
        showdate.setShow_date("2026-01-01");
        showdateRepo.save(showdate);

        Optional<Showdate> found = showdateRepo.findByShow_date("2026-01-01");

        assertTrue(found.isPresent());
        assertEquals("2026-01-01", found.get().getShow_date());
    }

    @Test
    void findByShow_date_noMatchingRow_returnsEmpty() {
        Optional<Showdate> found = showdateRepo.findByShow_date("1999-12-31");

        assertTrue(found.isEmpty());
    }
}
