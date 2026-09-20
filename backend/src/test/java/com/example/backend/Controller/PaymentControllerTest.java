package com.example.backend.Controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.Collections;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;

import com.example.backend.DTO.OrderDTO;
import com.example.backend.Entity.Order;
import com.example.backend.Entity.Role;
import com.example.backend.Entity.User;
import com.example.backend.Security.CustomUserDetails;
import com.example.backend.Service.PaymentService;

// Unit tests for the ownership check added to /api/orders/**: a caller may
// only read or create orders for their own userId, unless they're an
// ADMIN/MANAGER. PaymentService is mocked, nothing here touches a database.
@ExtendWith(MockitoExtension.class)
class PaymentControllerTest {

    @Mock
    private PaymentService paymentService;

    @InjectMocks
    private PaymentController paymentController;

    // Builds a fake, already-authenticated request from the given user.
    private Authentication authAs(Integer userId, Role role) {
        User user = new User();
        user.setUser_id(userId);
        user.setRole(role);

        Authentication authentication = mock(Authentication.class);
        when(authentication.getPrincipal()).thenReturn(new CustomUserDetails(user));
        return authentication;
    }

    // ---- getUserOrders ----

    @Test
    void getUserOrders_ownUserId_returnsOrders() {
        Authentication auth = authAs(1, Role.USER);
        when(paymentService.getOrdersByUserId(1)).thenReturn(Collections.emptyList());

        ResponseEntity<?> response = paymentController.getUserOrders(1, auth);

        assertEquals(200, response.getStatusCode().value());
    }

    @Test
    void getUserOrders_someoneElsesUserId_throwsAccessDenied() {
        Authentication auth = authAs(1, Role.USER); // logged in as user 1

        assertThrows(AccessDeniedException.class, () -> paymentController.getUserOrders(999, auth));
        verify(paymentService, never()).getOrdersByUserId(any());
    }

    @Test
    void getUserOrders_adminRequestingSomeoneElse_isAllowed() {
        Authentication auth = authAs(1, Role.ADMIN); // admin, but not user 999 themselves
        when(paymentService.getOrdersByUserId(999)).thenReturn(Collections.emptyList());

        ResponseEntity<?> response = paymentController.getUserOrders(999, auth);

        assertEquals(200, response.getStatusCode().value());
    }

    // ---- getOrderDetails ----

    @Test
    void getOrderDetails_orderDoesNotExist_returns404() {
        Authentication auth = authAs(1, Role.USER);
        when(paymentService.getOrderDetails(12345)).thenThrow(new RuntimeException("Order not found"));

        ResponseEntity<Order> response = paymentController.getOrderDetails(12345, auth);

        assertEquals(404, response.getStatusCode().value());
    }

    @Test
    void getOrderDetails_belongsToSomeoneElse_throwsAccessDenied() {
        Authentication auth = authAs(1, Role.USER); // logged in as user 1

        Order someoneElsesOrder = new Order();
        someoneElsesOrder.setOrderNumber(555);
        someoneElsesOrder.setUserId(2); // belongs to a different user

        when(paymentService.getOrderDetails(555)).thenReturn(someoneElsesOrder);

        assertThrows(AccessDeniedException.class, () -> paymentController.getOrderDetails(555, auth));
    }

    @Test
    void getOrderDetails_ownOrder_returnsIt() {
        Authentication auth = authAs(1, Role.USER);

        Order myOrder = new Order();
        myOrder.setOrderNumber(555);
        myOrder.setUserId(1);

        when(paymentService.getOrderDetails(555)).thenReturn(myOrder);

        ResponseEntity<Order> response = paymentController.getOrderDetails(555, auth);

        assertEquals(200, response.getStatusCode().value());
        assertEquals(555, response.getBody().getOrderNumber());
    }

    // ---- createPendingOrder ----

    @Test
    void createPendingOrder_userIdDoesNotMatchCaller_throwsAccessDenied() {
        Authentication auth = authAs(1, Role.USER); // logged in as user 1

        OrderDTO dto = new OrderDTO();
        dto.setUserId(2); // trying to create an order under someone else's id
        dto.setAmount(500);

        assertThrows(AccessDeniedException.class, () -> paymentController.createPendingOrder(dto, auth));
        // The ownership check should fail before paymentService is touched at all.
        verifyNoInteractions(paymentService);
    }

    @Test
    void createPendingOrder_ownUserId_createsOrder() {
        Authentication auth = authAs(1, Role.USER);

        OrderDTO dto = new OrderDTO();
        dto.setUserId(1);
        dto.setAmount(500);
        dto.setDescription("A1-1");
        dto.setItemName("Some Movie");

        when(paymentService.generateOrderNumber()).thenReturn(999999999);
        when(paymentService.addOrder(eq(999999999), eq(1), any(), eq(500), eq("A1-1"), eq("Some Movie"), eq(false)))
                .thenAnswer(invocation -> {
                    Order order = new Order();
                    order.setOrderNumber(invocation.getArgument(0));
                    order.setUserId(invocation.getArgument(1));
                    return order;
                });

        ResponseEntity<Order> response = paymentController.createPendingOrder(dto, auth);

        assertEquals(200, response.getStatusCode().value());
        assertEquals(999999999, response.getBody().getOrderNumber());
    }
}
