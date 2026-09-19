package com.example.backend.Controller;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.backend.DTO.TicketItemDTO;
import com.example.backend.Service.PaymentService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.stripe.Stripe;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.exception.StripeException;
import com.stripe.model.Event;
import com.stripe.model.checkout.Session;
import com.stripe.net.Webhook;
import com.stripe.param.checkout.SessionCreateParams;

import jakarta.annotation.PostConstruct;

@RestController
@RequestMapping("/api/stripe")
public class StripeController {

    @Autowired
    private PaymentService paymentService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${stripe.secret-key}")
    private String stripeSecretKey;

    @Value("${stripe.webhook-secret}")
    private String webhookSecret;

    @Value("${app.frontend-url:http://localhost:3000}")
    private String frontendUrl;

    @PostConstruct
    public void init() {
        Stripe.apiKey = stripeSecretKey;
    }

    public static class CheckoutItem {
        private String name;
        private long unitAmount;
        private long quantity;
        // New: which showtime and seats this line item covers, so a Ticket
        // can be created per seat once the webhook confirms payment.
        private Integer showtimeId;
        private List<String> seatNumbers;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public long getUnitAmount() {
            return unitAmount;
        }

        public void setUnitAmount(long unitAmount) {
            this.unitAmount = unitAmount;
        }

        public long getQuantity() {
            return quantity;
        }

        public void setQuantity(long quantity) {
            this.quantity = quantity;
        }

        public Integer getShowtimeId() {
            return showtimeId;
        }

        public void setShowtimeId(Integer showtimeId) {
            this.showtimeId = showtimeId;
        }

        public List<String> getSeatNumbers() {
            return seatNumbers;
        }

        public void setSeatNumbers(List<String> seatNumbers) {
            this.seatNumbers = seatNumbers;
        }
    }

    // New: carry along "who this order belongs to and what they bought" so that
    // when the payment-succeeded webhook fires, we know which order to update for which user.
    public static class CheckoutRequest {
        private Integer userId;
        private Integer orderNumber; // New: the real order number already created in Step 2
        private String description;
        private String itemName;
        private List<CheckoutItem> items;

        public Integer getUserId() {
            return userId;
        }

        public void setUserId(Integer userId) {
            this.userId = userId;
        }

        public void setOrderNumber(Integer orderNumber) {
            this.orderNumber = orderNumber;
        }

        public Integer getOrderNumber() {
            return orderNumber;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public String getItemName() {
            return itemName;
        }

        public void setItemName(String itemName) {
            this.itemName = itemName;
        }

        public List<CheckoutItem> getItems() {
            return items;
        }

        public void setItems(List<CheckoutItem> items) {
            this.items = items;
        }
    }

    @PostMapping("/create-checkout-session")
    public ResponseEntity<Map<String, String>> createCheckoutSession(@RequestBody CheckoutRequest request)
            throws StripeException {
        List<SessionCreateParams.LineItem> lineItems = request.getItems().stream()
                .map(item -> SessionCreateParams.LineItem.builder()
                        .setQuantity(item.getQuantity())
                        .setPriceData(
                                SessionCreateParams.LineItem.PriceData.builder()
                                        .setCurrency("aud")
                                        .setUnitAmount(item.getUnitAmount())
                                        .setProductData(
                                                SessionCreateParams.LineItem.PriceData.ProductData.builder()
                                                        .setName(item.getName())
                                                        .build())
                                        .build())
                        .build())
                .collect(Collectors.toList());

        // New: also carry each item's showtime + seats through as JSON, so the
        // webhook can create a Ticket per seat once payment is confirmed.
        String ticketItemsJson;
        try {
            ticketItemsJson = objectMapper.writeValueAsString(request.getItems());
        } catch (JsonProcessingException e) {
            ticketItemsJson = "[]";
        }

        // New: store userId, description, and itemName in the Stripe Session metadata.
        // Stripe keeps this data as-is and sends it back to us when payment completes.
        SessionCreateParams params = SessionCreateParams.builder()
                .setMode(SessionCreateParams.Mode.PAYMENT)
                .setSuccessUrl(frontendUrl + "/OrderList?stripe=success")
                .setCancelUrl(frontendUrl + "/CheckOutIn?stripe=cancel")
                .addAllLineItem(lineItems)
                .putMetadata("userId", String.valueOf(request.getUserId()))
                .putMetadata("orderNumber", String.valueOf(request.getOrderNumber()))
                .putMetadata("description", request.getDescription())
                .putMetadata("itemName", request.getItemName())
                .putMetadata("ticketItems", ticketItemsJson)
                .build();

        Session session = Session.create(params);

        return ResponseEntity.ok(Map.of("url", session.getUrl()));
    }

    // New: Stripe calls this endpoint itself whenever a payment's status changes
    // (it's not something the frontend calls).
    @PostMapping("/webhook")
    public ResponseEntity<String> handleWebhook(
            @RequestBody String payload,
            @RequestHeader("Stripe-Signature") String sigHeader) {

        Event event;
        try {
            // Verify this request genuinely came from Stripe (checked via the webhook secret's signature),
            // to stop someone from forging orders by impersonating Stripe against this endpoint.
            event = Webhook.constructEvent(payload, sigHeader, webhookSecret);
        } catch (SignatureVerificationException e) {
            System.err.println("Stripe webhook signature verification failed: " + e.getMessage());
            return ResponseEntity.status(400).body("Invalid signature");
        }

        if ("checkout.session.completed".equals(event.getType())) {
            event.getDataObjectDeserializer().getObject().ifPresent(obj -> {
                Session session = (Session) obj;
                Map<String, String> metadata = session.getMetadata();

                Integer userId = Integer.valueOf(metadata.getOrDefault("userId", "0"));
                String description = metadata.get("description");
                String itemName = metadata.get("itemName");
                Integer amount = (int) (session.getAmountTotal() / 100); // Stripe amounts are in cents; convert back to dollars

                Integer orderNumber = Integer.valueOf(metadata.get("orderNumber"));
                paymentService.updateOrder(orderNumber, LocalDateTime.now(), amount, description, itemName, true);

                // New: now that payment is confirmed, create the actual Ticket
                // rows (one per seat) so Order Lookup has something to show.
                try {
                    List<TicketItemDTO> ticketItems = objectMapper.readValue(
                            metadata.getOrDefault("ticketItems", "[]"),
                            new TypeReference<List<TicketItemDTO>>() {});
                    paymentService.createTicketsForOrder(orderNumber, ticketItems);
                } catch (JsonProcessingException e) {
                    System.err.println("Could not parse ticketItems metadata for order " + orderNumber + ": " + e.getMessage());
                }
            });
        }

        return ResponseEntity.ok("received");
    }
}
