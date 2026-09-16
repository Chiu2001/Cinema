package com.example.backend.Controller;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.backend.Entity.Order;
import com.example.backend.Service.PaymentService;

@RestController
@RequestMapping("/ecpay")
@CrossOrigin(origins = "*", maxAge = 3600)
public class PaymentResultController {

	@Autowired
	private PaymentService payService;

	@PostMapping("/paymentResult")
	public ResponseEntity<String> handleServerPostRequest(@RequestParam Map<String, String> params) {
		// Get the parameters from the request
		String merchantTradeNo = params.get("MerchantTradeNo");
		String merchantTradeDate = params.get("MerchantTradeDate");

		// Debug log for incoming parameters
		System.out.println("Received MerchantTradeNo (Server POST): " + merchantTradeNo);
		System.out.println("Received MerchantTradeDate (Server POST): " + merchantTradeDate);

		// Check whether the required parameters are missing
		if (merchantTradeNo == null || merchantTradeDate == null) {
			return ResponseEntity.badRequest().body("Request is missing the required MerchantTradeNo or MerchantTradeDate parameter");
		}

		// Convert MerchantTradeDate into a LocalDateTime
		LocalDateTime parsedMerchantTradeDate = null;
		try {
			DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
			parsedMerchantTradeDate = LocalDateTime.parse(merchantTradeDate, formatter);
		} catch (DateTimeParseException e) {
			return ResponseEntity.badRequest().body("Invalid MerchantTradeDate format, expected: yyyy/MM/dd HH:mm:ss");
		}

		try {
			// Get the order record
			Order orderData = payService.getOrderDetails(Integer.parseInt(merchantTradeNo));

			// Update the payment data
			payService.updatePaydata(orderData, parsedMerchantTradeDate, "ECPay", "Paid");

			// Return 1|OK to ECPay to indicate the request was processed successfully
			return ResponseEntity.ok("1|OK");
		} catch (Exception e) {
			e.printStackTrace();
			return ResponseEntity.status(500).body("An error occurred while updating the payment data");
		}
	}

	@PostMapping("/orderResult")
    public ResponseEntity<String> handleClientPostRequest(@RequestParam Map<String, String> params) {
        // Get the parameters from the request
        String merchantTradeNo = params.get("MerchantTradeNo");
        String merchantTradeDate = params.get("MerchantTradeDate");

        // Debug log for incoming parameters
        System.out.println("Received MerchantTradeNo (Client POST): " + merchantTradeNo);
        System.out.println("Received MerchantTradeDate (Client POST): " + merchantTradeDate);

        // Check whether the required parameters are missing
        if (merchantTradeNo == null || merchantTradeDate == null) {
            return ResponseEntity.badRequest().body("Request is missing the required MerchantTradeNo or MerchantTradeDate parameter");
        }

        // Convert MerchantTradeDate into a LocalDateTime
        LocalDateTime parsedMerchantTradeDate = null;
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
            parsedMerchantTradeDate = LocalDateTime.parse(merchantTradeDate, formatter);
        } catch (DateTimeParseException e) {
            return ResponseEntity.badRequest().body("Invalid MerchantTradeDate format, expected: yyyy/MM/dd HH:mm:ss");
        }

        try {

        	Order order = payService.getOrderDetails(Integer.parseInt(merchantTradeNo));

            // Update the order status to paid
            payService.updateOrder(order.getOrderNumber(), null, null, null, null, true);
            // Build the redirect URL to the frontend React page, including the transaction parameters
            String redirectUrl = "http://localhost:3000/PaymentResultPage?MerchantTradeNo=" + merchantTradeNo
                                 + "&MerchantTradeDate=" + merchantTradeDate;
            String htmlResponse = "<html><body><script>window.location.href = '" + redirectUrl + "';</script></body></html>";
            return ResponseEntity.ok(htmlResponse);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("An error occurred while processing the client request");
        }
    }
}