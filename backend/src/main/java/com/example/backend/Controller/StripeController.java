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

import com.example.backend.Service.PaymentService;
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
    }

    // 新增：把「這筆訂單要記錄成誰買的、買了什麼」一起帶過來，
    // 之後付款成功的 webhook 觸發時，才知道要幫誰建立哪一筆訂單。
    public static class CheckoutRequest {
        private Integer userId;
        private Integer orderNumber; // 新增：帶著 Step 2 已經建立好的真實訂單編號
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

        // 新增：把 userId、description、itemName 存進 Stripe Session 的 metadata，
        // 這些資料 Stripe 會原封不動存著，付款完成通知我們時會一起傳回來。
        SessionCreateParams params = SessionCreateParams.builder()
                .setMode(SessionCreateParams.Mode.PAYMENT)
                .setSuccessUrl(frontendUrl + "/OrderList?stripe=success")
                .setCancelUrl(frontendUrl + "/CheckOutIn?stripe=cancel")
                .addAllLineItem(lineItems)
                .putMetadata("userId", String.valueOf(request.getUserId()))
                .putMetadata("orderNumber", String.valueOf(request.getOrderNumber()))
                .putMetadata("description", request.getDescription())
                .putMetadata("itemName", request.getItemName())
                .build();

        Session session = Session.create(params);

        return ResponseEntity.ok(Map.of("url", session.getUrl()));
    }

    // 新增：Stripe 付款狀態變化時，會主動呼叫這支 API 通知我們（不是前端呼叫的）。
    @PostMapping("/webhook")
    public ResponseEntity<String> handleWebhook(
            @RequestBody String payload,
            @RequestHeader("Stripe-Signature") String sigHeader) {

        Event event;
        try {
            // 驗證這個請求真的是 Stripe 發的（用 webhook secret 驗簽章），
            // 避免有心人士假冒 Stripe 打這支 API 來偽造訂單。
            event = Webhook.constructEvent(payload, sigHeader, webhookSecret);
        } catch (SignatureVerificationException e) {
            System.err.println("Stripe webhook 簽章驗證失敗：" + e.getMessage());
            return ResponseEntity.status(400).body("Invalid signature");
        }

        if ("checkout.session.completed".equals(event.getType())) {
            event.getDataObjectDeserializer().getObject().ifPresent(obj -> {
                Session session = (Session) obj;
                Map<String, String> metadata = session.getMetadata();

                Integer userId = Integer.valueOf(metadata.getOrDefault("userId", "0"));
                String description = metadata.get("description");
                String itemName = metadata.get("itemName");
                Integer amount = (int) (session.getAmountTotal() / 100); // Stripe 金額單位是分，換回元

                Integer orderNumber = Integer.valueOf(metadata.get("orderNumber"));
paymentService.updateOrder(orderNumber, LocalDateTime.now(), amount, description, itemName, true);
            });
        }

        return ResponseEntity.ok("received");
    }
}
