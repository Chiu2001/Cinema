package com.example.backend.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.backend.Entity.Order;
import com.example.backend.Repo.OrderRepo;

@Service
public class PaymentService {

    @Autowired
    private OrderRepo orderRepo;

    private static final String PAYMENT_SUCCESS = "Paid"; // Defines the "payment successful" status

    public Integer generateOrderNumber() {
        // Generate a 10-digit random integer as the order number
        Random random = new Random();
        return 100000000 + random.nextInt(900000000);
    }

    // Create a new order
    public Order addOrder(Integer orderNumber, Integer userId, LocalDateTime createdDate, Integer amount, String description, String itemName, Boolean isPaid) {
        // Pass all required fields when creating the object using the existing constructor
        Order newOrder = new Order();
        newOrder.setOrderNumber(orderNumber); // Set the generated order number
        newOrder.setUserId(userId);
        newOrder.setCreatedDate(createdDate);
        newOrder.setAmount(amount);
        newOrder.setDescription(description);
        newOrder.setItemName(itemName);
        newOrder.setIsPaid(isPaid);

        return orderRepo.save(newOrder); // Save to the database
    }

    // Update an order
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
        orderData.setDescription(payStatus); // Update the description with the payment status
        orderData.setCreatedDate(merchantTradeDate); // Update the order date
        orderRepo.save(orderData); // Save the updated data
    }

    public List<Order> getOrdersByUserId(Integer userId) {
        return orderRepo.findByUserId(userId); // Get orders by user ID
    }
}
