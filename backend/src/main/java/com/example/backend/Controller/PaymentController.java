package com.example.backend.Controller;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.backend.DTO.OrderDTO;
import com.example.backend.Entity.Order;
import com.example.backend.Entity.Role;
import com.example.backend.Entity.User;
import com.example.backend.Repo.OrderRepo;
import com.example.backend.Security.CustomUserDetails;
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
    public ResponseEntity<List<Order>> getUserOrders(@RequestParam Integer userId, Authentication authentication) {
        requireSelfOrAdmin(authentication, userId);
        List<Order> userOrders = paymentService.getOrdersByUserId(userId);
        return ResponseEntity.ok(userOrders);
    }

    // Get order details
    @GetMapping("/details")
    public ResponseEntity<Order> getOrderDetails(@RequestParam Integer orderNumber, Authentication authentication) {
        Order order;
        try {
            order = paymentService.getOrderDetails(orderNumber);
        } catch (Exception e) {
            return ResponseEntity.status(404).body(null); // Handle order-not-found case
        }
        requireSelfOrAdmin(authentication, order.getUserId());
        return ResponseEntity.ok(order);
    }

    // New: create a pending order as soon as checkout is confirmed, with status "unpaid",
    // so the order already exists no matter which payment method the user ends up choosing;
    // afterwards we only need to update its payment status.
    @PostMapping("/create-pending")
    public ResponseEntity<Order> createPendingOrder(@RequestBody OrderDTO orderDTO, Authentication authentication) {
        requireSelfOrAdmin(authentication, orderDTO.getUserId());
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

    // These endpoints used to have no auth requirement at all (see
    // SecurityConfiguration), so anyone could read or create orders for any
    // userId just by changing a query/body parameter. This confirms the
    // caller is either that same user, or an ADMIN/MANAGER.
    private void requireSelfOrAdmin(Authentication authentication, Integer userId) {
        if (!(authentication.getPrincipal() instanceof CustomUserDetails callerDetails)) {
            throw new AccessDeniedException("You do not have permission to perform this action");
        }
        User caller = callerDetails.getUser();
        boolean isSelf = caller.getUser_id().equals(userId);
        boolean isAdminOrManager = caller.getRole() == Role.ADMIN || caller.getRole() == Role.MANAGER;
        if (!isSelf && !isAdminOrManager) {
            throw new AccessDeniedException("You do not have permission to perform this action");
        }
    }
}
