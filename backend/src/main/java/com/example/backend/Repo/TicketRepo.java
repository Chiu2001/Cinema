package com.example.backend.Repo;

import org.springframework.data.jpa.repository.JpaRepository;
import com.example.backend.Entity.Ticket;

public interface TicketRepo extends JpaRepository<Ticket, Long> {

    // Used by PaymentService to make webhook processing idempotent: Stripe
    // can and does redeliver the same checkout.session.completed event, so
    // this checks whether tickets were already created for this order
    // before creating them again.
    boolean existsByOrder(int orderNumber);
}
