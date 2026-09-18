package com.example.backend.Controller;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.backend.Service.LinePayService;
import com.example.backend.Service.PaymentService;

@RestController
@RequestMapping("/checkout")
public class LinePayController {

    @Autowired
    private LinePayService service;

    @Autowired
    private PaymentService paymentService;

    // Save the payment request
    @PostMapping("/save")
    public ResponseEntity<Map<String, Object>> saveCheckoutPaymentRequest(@RequestBody Map<String, Object> requestBody) {
        service.saveCheckoutPaymentRequest(requestBody);

        // Build the response Map
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Order saved successfully");
        response.put("orderId", requestBody.get("orderId"));  // Return the ID of the order just saved

        return ResponseEntity.ok(response);
    }

    // Get the payment details for the given ID
    @GetMapping("/details/{orderId}")
    public ResponseEntity<?> getCheckoutPaymentDetails(@PathVariable String orderId) {

        Map<String, Object> response = service.getCheckoutPaymentDetails(orderId);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    // Send the payment request to the LINE Pay API
    @PostMapping("/payment")
    public ResponseEntity<?> processPaymentRequest(@RequestBody Map<String, Object> requestBody) {
        // Pass the incoming request body to the service for processing
        Map<String, Object> response = service.sendPaymentRequest(requestBody);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    // This is the confirmUrl LINE Pay redirects the user's browser to after they
    // approve payment on LINE Pay's hosted page. Confirming (capturing) the
    // payment has to happen here, server-side, since it needs the channel
    // secret; nothing previously called this at all, so a LINE Pay payment
    // could be approved by the user and the order would still never be marked
    // paid.
    @GetMapping("/confirm")
    public ResponseEntity<Void> confirmPayment(@RequestParam String transactionId, @RequestParam String orderId) {
        boolean confirmed = service.confirmPayment(transactionId, orderId);

        if (confirmed) {
            try {
                paymentService.updateOrder(Integer.parseInt(orderId), null, null, null, null, true);
            } catch (NumberFormatException | RuntimeException e) {
                e.printStackTrace();
                confirmed = false;
            }
        }

        String redirectUrl = "http://localhost:3000/PaymentResultPage?MerchantTradeNo=" + orderId
                + "&status=" + (confirmed ? "success" : "failed");
        return ResponseEntity.status(HttpStatus.FOUND).location(URI.create(redirectUrl)).build();
    }

}