package com.example.backend.Service.Impl;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.Mockito;

import com.example.backend.DTO.SeatDTO;
import com.example.backend.Entity.Cinema;
import com.example.backend.Entity.Hall;
import com.example.backend.Entity.Seat;
import com.example.backend.Entity.Showtime;
import com.example.backend.Repo.SeatRepo;
import com.example.backend.Repo.ShowtimeRepo;

// Unit tests for the reservedAt bookkeeping behind the seat-reservation TTL:
// reserving a seat must stamp when the hold started, releasing it must
// clear that stamp, so SeatReservationCleanupTask knows which seats are
// actually still held versus available or permanently sold.
@ExtendWith(MockitoExtension.class)
class SeatServiceImplTest {

    @Mock
    private SeatRepo seatRepo;

    @Mock
    private ShowtimeRepo showtimeRepo;

    @InjectMocks
    private SeatServiceImpl seatService;

    private Showtime showtimeWithCinemaAndHall() {
        Showtime showtime = Mockito.mock(Showtime.class);
        when(showtime.getCinema()).thenReturn(new Cinema());
        when(showtime.getHall()).thenReturn(new Hall());
        return showtime;
    }

    @Test
    void addSeat_reservingNewSeat_stampsReservedAt() {
        Showtime showtime = showtimeWithCinemaAndHall();
        when(showtimeRepo.findById(1)).thenReturn(Optional.of(showtime));
        when(seatRepo.findByShowtimeIdAndCinemaIdAndHallIdAndSeatNumber(any(), any(), any(), any()))
                .thenReturn(null); // seat doesn't exist yet

        SeatDTO dto = new SeatDTO("A1-1", false, 1, 1, 1);
        seatService.addSeat(dto);

        ArgumentCaptor<Seat> captor = ArgumentCaptor.forClass(Seat.class);
        Mockito.verify(seatRepo).save(captor.capture());
        assertNotNull(captor.getValue().getReservedAt());
    }

    @Test
    void addSeat_releasingExistingSeat_clearsReservedAt() {
        Showtime showtime = showtimeWithCinemaAndHall();
        when(showtimeRepo.findById(1)).thenReturn(Optional.of(showtime));

        Seat existingSeat = new Seat();
        existingSeat.setSeatNumber("A1-1");
        existingSeat.setSeatAvailability(false);
        existingSeat.setReservedAt(LocalDateTime.now().minusMinutes(5));
        when(seatRepo.findByShowtimeIdAndCinemaIdAndHallIdAndSeatNumber(any(), any(), any(), any()))
                .thenReturn(existingSeat);

        SeatDTO dto = new SeatDTO("A1-1", true, 1, 1, 1); // releasing it
        seatService.addSeat(dto);

        assertNull(existingSeat.getReservedAt());
    }

    @Test
    void addSeat_alreadyReservedByAnotherBooking_throwsIllegalStateException() {
        Showtime showtime = showtimeWithCinemaAndHall();
        when(showtimeRepo.findById(1)).thenReturn(Optional.of(showtime));

        Seat existingSeat = new Seat();
        existingSeat.setSeatNumber("A1-1");
        existingSeat.setSeatAvailability(false); // someone else already has it

        when(seatRepo.findByShowtimeIdAndCinemaIdAndHallIdAndSeatNumber(any(), any(), any(), any()))
                .thenReturn(existingSeat);

        SeatDTO dto = new SeatDTO("A1-1", false, 1, 1, 1); // trying to reserve it too

        assertThrows(IllegalStateException.class, () -> seatService.addSeat(dto));
    }
}
