package com.example.backend.Service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.example.backend.Entity.Seat;
import com.example.backend.Repo.SeatRepo;

// Selecting a seat marks it unavailable immediately (see SeatServiceImpl),
// so other users don't get to pick it too. If the person then abandons the
// cart — closes the tab, never pays, doesn't click "Remove" — that seat used
// to stay locked forever, since nothing ever released it. This periodically
// releases any hold older than the TTL back to available.
@Component
public class SeatReservationCleanupTask {

    @Autowired
    private SeatRepo seatRepo;

    @Value("${seat.reservation.ttl-minutes:10}")
    private long ttlMinutes;

    // Runs once a minute; each run only touches holds that are already
    // past the TTL, so a shorter or longer interval just changes how
    // promptly an expired hold gets released, not correctness.
    @Scheduled(fixedRate = 60_000)
    public void releaseExpiredHolds() {
        LocalDateTime cutoff = LocalDateTime.now().minusMinutes(ttlMinutes);
        List<Seat> expiredHolds = seatRepo.findBySeatAvailabilityFalseAndReservedAtBefore(cutoff);

        for (Seat seat : expiredHolds) {
            seat.setSeatAvailability(true);
            seat.setReservedAt(null);
        }

        if (!expiredHolds.isEmpty()) {
            seatRepo.saveAll(expiredHolds);
        }
    }
}
