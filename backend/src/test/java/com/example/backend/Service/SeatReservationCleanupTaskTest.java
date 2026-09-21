package com.example.backend.Service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.example.backend.Entity.Seat;
import com.example.backend.Repo.SeatRepo;

// Unit tests for the seat-reservation TTL sweep. SeatRepo is mocked, so
// these only check what releaseExpiredHolds() would do with whatever the
// repository query returns — not the query itself (that would need a real
// database, like ShowdateRepoTest).
@ExtendWith(MockitoExtension.class)
class SeatReservationCleanupTaskTest {

    @Mock
    private SeatRepo seatRepo;

    @InjectMocks
    private SeatReservationCleanupTask cleanupTask;

    @Test
    void releaseExpiredHolds_seatsPastTtl_releasesAndClearsThem() {
        Seat expired = new Seat();
        expired.setSeatNumber("A1-1");
        expired.setSeatAvailability(false);
        expired.setReservedAt(LocalDateTime.now().minusMinutes(30));

        when(seatRepo.findBySeatAvailabilityFalseAndReservedAtBefore(any())).thenReturn(List.of(expired));

        cleanupTask.releaseExpiredHolds();

        assertTrue(expired.getSeatAvailability());
        assertNull(expired.getReservedAt());
        verify(seatRepo).saveAll(anyList());
    }

    @Test
    void releaseExpiredHolds_nothingExpired_doesNotTouchTheDatabase() {
        when(seatRepo.findBySeatAvailabilityFalseAndReservedAtBefore(any())).thenReturn(Collections.emptyList());

        cleanupTask.releaseExpiredHolds();

        verify(seatRepo, never()).saveAll(anyList());
    }

    @Test
    void releaseExpiredHolds_multipleSeats_releasesAllOfThem() {
        Seat first = new Seat();
        first.setSeatAvailability(false);
        first.setReservedAt(LocalDateTime.now().minusMinutes(15));

        Seat second = new Seat();
        second.setSeatAvailability(false);
        second.setReservedAt(LocalDateTime.now().minusMinutes(45));

        when(seatRepo.findBySeatAvailabilityFalseAndReservedAtBefore(any())).thenReturn(List.of(first, second));

        cleanupTask.releaseExpiredHolds();

        assertEquals(true, first.getSeatAvailability());
        assertEquals(true, second.getSeatAvailability());
        assertNull(first.getReservedAt());
        assertNull(second.getReservedAt());
    }
}
