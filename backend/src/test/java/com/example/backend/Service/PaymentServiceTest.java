package com.example.backend.Service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.example.backend.DTO.TicketItemDTO;
import com.example.backend.Entity.Ticket;
import com.example.backend.Repo.TicketRepo;

// Unit tests for PaymentService.createTicketsForOrder — the method behind
// yesterday's "Order Lookup stays empty" bug. TicketRepo is mocked, nothing
// here touches a database; these tests only check that the right Ticket
// objects would be saved.
@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

    @Mock
    private TicketRepo ticketRepo;

    @InjectMocks
    private PaymentService paymentService;

    private TicketItemDTO ticketItem(Integer showtimeId, List<String> seatNumbers, Integer unitAmountCents) {
        TicketItemDTO item = new TicketItemDTO();
        item.setShowtimeId(showtimeId);
        item.setSeatNumbers(seatNumbers);
        item.setUnitAmount(unitAmountCents);
        return item;
    }

    @Test
    void createTicketsForOrder_oneItemTwoSeats_savesOneTicketPerSeat() {
        TicketItemDTO item = ticketItem(6, Arrays.asList("D1-5", "D1-6"), 16000); // 160.00 per seat

        paymentService.createTicketsForOrder(774520727, List.of(item));

        ArgumentCaptor<Ticket> captor = ArgumentCaptor.forClass(Ticket.class);
        verify(ticketRepo, times(2)).save(captor.capture());

        List<Ticket> savedTickets = captor.getAllValues();
        assertEquals(2, savedTickets.size());

        Ticket first = savedTickets.get(0);
        assertEquals(774520727, first.getOrder());
        assertEquals(6, first.getShowtime());
        assertEquals("D1-5", first.getSeat());
        assertEquals(160, first.getPrice()); // cents -> dollars

        Ticket second = savedTickets.get(1);
        assertEquals("D1-6", second.getSeat());
    }

    @Test
    void createTicketsForOrder_multipleItems_savesTicketsForEachSeatOfEach() {
        TicketItemDTO item1 = ticketItem(6, List.of("D1-5"), 16000);
        TicketItemDTO item2 = ticketItem(9, Arrays.asList("A1-2", "A1-3"), 28000);

        paymentService.createTicketsForOrder(123, Arrays.asList(item1, item2));

        // 1 seat from item1 + 2 seats from item2 = 3 tickets total.
        verify(ticketRepo, times(3)).save(any(Ticket.class));
    }

    @Test
    void createTicketsForOrder_noItems_savesNothing() {
        paymentService.createTicketsForOrder(123, Collections.emptyList());

        verify(ticketRepo, never()).save(any());
    }
}
