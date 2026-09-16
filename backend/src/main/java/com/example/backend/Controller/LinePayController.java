package com.example.backend.Controller;

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
import org.springframework.web.bind.annotation.RestController;

import com.example.backend.Service.LinePayService;

@RestController
@RequestMapping("/checkout")
public class LinePayController {

    @Autowired
    private LinePayService service;

    // Save the payment request
    @PostMapping("/save")
    public ResponseEntity<Map<String, Object>> saveCheckoutPaymentRequest(@RequestBody Map<String, Object> requestBody) {
        service.saveCheckoutPaymentRequest(requestBody);

        // Build the response Map
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Order saved successfully");
        response.put("orderId", requestBody.get("orderId"));  // Return the ID of the order that was just saved

        return ResponseEntity.ok(response);
    }

    // Get the payment details for the specified ID
    @GetMapping("/details/{orderId}")
    public ResponseEntity<?> getCheckoutPaymentDetails(@PathVariable String orderId) {

        Map<String, Object> response = service.getCheckoutPaymentDetails(orderId);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    // Send the payment request to the Line Pay API
    @PostMapping("/payment")
    public ResponseEntity<?> processPaymentRequest(@RequestBody Map<String, Object> requestBody) {
        // Pass the incoming request body to the service for processing
        Map<String, Object> response = service.sendPaymentRequest(requestBody);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }
    
}