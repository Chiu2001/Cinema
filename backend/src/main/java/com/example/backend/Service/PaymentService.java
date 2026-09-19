package com.example.backend.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.backend.DTO.TicketItemDTO;
import com.example.backend.Entity.Order;
import com.example.backend.Entity.Ticket;
import com.example.backend.Repo.OrderRepo;
import com.example.backend.Repo.TicketRepo;

@Service
public class PaymentService {

    @Autowired
    private OrderRepo orderRepo;

    @Autowired
    private TicketRepo ticketRepo;

    private static final String PAYMENT_SUCCESS = "Paid"; // Defines the "payment succeeded" status

    public Integer generateOrderNumber() {
        // Generate a 10-digit random integer to use as the order number
        Random random = new Random();
        return 100000000 + random.nextInt(900000000);
    }

    // Create a new order
    public Order addOrder(Integer orderNumber, Integer userId, LocalDateTime createdDate, Integer amount, String description, String itemName, Boolean isPaid) {
        // Populate every required field when constructing the object
        Order newOrder = new Order();
        newOrder.setOrderNumber(orderNumber); // Set the generated order number
        newOrder.setUserId(userId);
        newOrder.setCreatedDate(createdDate);
        newOrder.setAmount(amount);
        newOrder.setDescription(description);
        newOrder.setItemName(itemName);
        newOrder.setIsPaid(isPaid);

        return orderRepo.save(newOrder); // Persist to the database
    }

    // Update an existing order
    public Order updateOrder(Integer orderNumber, LocalDateTime createdDate, Integer amount, String description, String itemName, Boolean isPaid) {
    	Order existingOrder = orderRepo.findByOrderNumber(orderNumber)
                .orElseThrow(() -> new RuntimeException("Order not found, order number: " + orderNumber));

        if (createdDate != null) existingOrder.setCreatedDate(createdDate);
        if (amount != null) existingOrder.setAmount(amount);
        if (description != null) existingOrder.setDescription(description);
        if (itemName != null) existingOrder.setItemName(itemName);
        if (isPaid != null) existingOrder.setIsPaid(isPaid);

        return orderRepo.save(existingOrder);
    }

    public Order getOrderDetails(Integer orderNumber) {
        return orderRepo.findByOrderNumber(orderNumber)
                .orElseThrow(() -> new RuntimeException("Order not found, order number: " + orderNumber));
    }

    // Update payment data
    public void updatePaydata(Order orderData, LocalDateTime merchantTradeDate, String payMethod, String payStatus) {
        orderData.setIsPaid(PAYMENT_SUCCESS.equals(payStatus)); // Update the paid status based on the payment status
        orderData.setDescription(payStatus); // Update the description to the payment status
        orderData.setCreatedDate(merchantTradeDate); // Update the order date
        orderRepo.save(orderData); // Save the updated data
    }

    public List<Order> getOrdersByUserId(Integer userId) {
        return orderRepo.findByUserId(userId); // Fetch orders by user ID
    }

    // Create one Ticket row per seat once payment is confirmed, so Order
    // Lookup has something to show. Called from the Stripe webhook handler.
    public void createTicketsForOrder(Integer orderNumber, List<TicketItemDTO> items) {
        LocalDateTime now = LocalDateTime.now();

        for (TicketItemDTO item : items) {
            int pricePerSeat = item.getUnitAmount() / 100; // cents -> dollars

            for (String seatNumber : item.getSeatNumbers()) {
                Ticket ticket = new Ticket();
                ticket.setOrder(orderNumber);
                ticket.setShowtime(item.getShowtimeId());
                ticket.setSeat(seatNumber);
                ticket.setPrice(pricePerSeat);
                ticket.setPurchasetime(now);
                ticketRepo.save(ticket);
            }
        }
    }
}
