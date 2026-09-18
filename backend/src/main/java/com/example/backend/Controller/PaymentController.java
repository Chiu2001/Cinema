package com.example.backend.Controller;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.backend.DTO.OrderDTO;
import com.example.backend.Entity.Order;
import com.example.backend.Repo.OrderRepo;
import com.example.backend.Service.PaymentService;

@RestController
@RequestMapping("/api/orders")
public class PaymentController {

    @Autowired
    private PaymentService paymentService;

    @Autowired
    OrderRepo orderRepo;

    // Get the order history for a given user
    @GetMapping("/user")
    public ResponseEntity<List<Order>> getUserOrders(@RequestParam Integer userId) {
        List<Order> userOrders = paymentService.getOrdersByUserId(userId);
        return ResponseEntity.ok(userOrders);
    }

    // Get order details
    @GetMapping("/details")
    public ResponseEntity<Order> getOrderDetails(@RequestParam Integer orderNumber) {
        try {
            Order order = paymentService.getOrderDetails(orderNumber);
            return ResponseEntity.ok(order);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(404).body(null); // Handle order-not-found case
        }
    }

    // New: create a pending order as soon as checkout is confirmed, with status "unpaid",
    // so the order already exists no matter which payment method the user ends up choosing;
    // afterwards we only need to update its payment status.
    @PostMapping("/create-pending")
    public ResponseEntity<Order> createPendingOrder(@RequestBody OrderDTO orderDTO) {
        Integer orderNumber = paymentService.generateOrderNumber();

        Order order = paymentService.addOrder(
                orderNumber,
                orderDTO.getUserId(),
                LocalDateTime.now(),
                orderDTO.getAmount(),
                orderDTO.getDescription(),
                orderDTO.getItemName(),
                false // unpaid
        );

        return ResponseEntity.ok(order);
    }
}
