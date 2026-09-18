package com.example.backend.Service;

import java.util.Map;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Optional;
import java.util.List;
import java.util.stream.Collectors;
import java.util.UUID;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.digest.HmacAlgorithms;
import org.apache.commons.codec.digest.HmacUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import com.example.backend.Entity.LinePayCheckoutPaymentRequestForm;
import com.example.backend.Entity.LinePayProductForm;
import com.example.backend.Entity.LinePayProductPackageForm;
import com.example.backend.Entity.LinePayRedirectUrls;
import com.example.backend.Repo.LinePayCheckoutPaymentRequestFormRepo;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class LinePayService {
    @Autowired
    private LinePayCheckoutPaymentRequestFormRepo repository;

    @Autowired
    private RestTemplate restTemplate;

    @Value("${Linepay.api.ChannelId}")
    private String ChannelId;

    @Value("${Linepay.api.ChannelSecret}")
    private String ChannelSecret;

    // Save to the database
    public void saveCheckoutPaymentRequest(Map<String, Object> requestBody) {
        LinePayCheckoutPaymentRequestForm checkoutForm = new LinePayCheckoutPaymentRequestForm();

        checkoutForm.setAmount(new BigDecimal((Integer) requestBody.get("amount")));
        checkoutForm.setOrderId((String) requestBody.get("orderId"));
        checkoutForm.setCurrency((String) requestBody.get("currency"));

        LinePayRedirectUrls redirectUrls = new LinePayRedirectUrls(); 
        redirectUrls.setConfirmUrl(requestBody.get("confirmUrl").toString());
        checkoutForm.setRedirectUrls(redirectUrls); 

        List<Map<String, Object>> packagesData = (List<Map<String, Object>>) requestBody.get("packages");

        List<LinePayProductPackageForm> packages = packagesData.stream().map(packageData -> {
            LinePayProductPackageForm productPackage = new LinePayProductPackageForm();
            productPackage.setName((String) packageData.get("name"));
            productPackage.setAmount(new BigDecimal((Integer) packageData.get("amount")));

            List<Map<String, Object>> productsData = (List<Map<String, Object>>) packageData.get("products");

            List<LinePayProductForm> products = productsData.stream().map(productData -> {
                LinePayProductForm productForm = new LinePayProductForm();
                productForm.setName((String) productData.get("name"));
                productForm.setQuantity(new BigDecimal((Integer) productData.get("quantity")));
                productForm.setPrice(new BigDecimal((Integer) productData.get("price")));
                return productForm;
            }).collect(Collectors.toList());

            productPackage.setProducts(products);
            return productPackage;
        }).collect(Collectors.toList());

        checkoutForm.setPackages(packages);
        repository.save(checkoutForm);
    }

    public static String encrypt(final String keys, final String data) {
        // HmacUtils.getHmacSha256 is deprecated, so use HmacUtils.getInitializedMac instead
        return toBase64String(HmacUtils.getInitializedMac(
                HmacAlgorithms.HMAC_SHA_256,
                keys.getBytes())
                .doFinal(data.getBytes()));
    }

    public static String toBase64String(byte[] bytes) {
        byte[] byteArray = Base64.encodeBase64(bytes);
        return new String(byteArray);
    }

    // Query the database so the data can be passed to the controller and the frontend
    public Map<String, Object> getCheckoutPaymentDetails(String orderId) {

        Optional<LinePayCheckoutPaymentRequestForm> optionalForm = repository.findByOrderId(orderId);
        // Use LinkedHashMap instead of HashMap because JSON key order matters when the data
        // is encrypted or verified using HMAC / digital signatures
        Map<String, Object> result = new LinkedHashMap<>();

        if (optionalForm.isPresent()) {
            LinePayCheckoutPaymentRequestForm form = optionalForm.get();

            // Get the basic information
            result.put("amount", form.getAmount());
            result.put("currency", form.getCurrency());
            result.put("orderId", form.getOrderId());

            // Get the RedirectUrls
            LinePayRedirectUrls redirectUrls = form.getRedirectUrls();
            if (redirectUrls != null) {
                Map<String, Object> redirectUrlsMap = new LinkedHashMap<>();
                redirectUrlsMap.put("confirmUrl", redirectUrls.getConfirmUrl());
                redirectUrlsMap.put("cancelUrl", null);// Default to null for now
                result.put("redirectUrls", redirectUrlsMap);
            } else {
                result.put("error", "RedirectUrls is null for CheckoutPaymentRequestForm ID: " + orderId);
            }

            // Get the ProductPackageForm and ProductForm entries
            List<Map<String, Object>> productPackages = form.getPackages().stream()
                    .map(packageForm -> {
                        Map<String, Object> packageDetails = new LinkedHashMap<>();
                        String packageId = String.valueOf(packageForm.getId());
                        packageDetails.put("id", packageId);
                        packageDetails.put("name", packageForm.getName());// Not required by the LINE Pay API
                        packageDetails.put("amount", packageForm.getAmount());

                        // Get the ProductForm entries
                        List<Map<String, Object>> products = packageForm.getProducts().stream()
                                .map(productForm -> {
                                    Map<String, Object> productDetails = new LinkedHashMap<>();
                                    String productId = String.valueOf(productForm.getId());
                                    productDetails.put("id", productId);
                                    productDetails.put("name", productForm.getName());
                                    productDetails.put("imageUrl", productForm.getImageUrl());
                                    productDetails.put("quantity", productForm.getQuantity());
                                    productDetails.put("price", productForm.getPrice());
                                    return productDetails;
                                })
                                .collect(Collectors.toList());

                        packageDetails.put("products", products);
                        return packageDetails;
                    })
                    .collect(Collectors.toList());

            result.put("packages", productPackages);

        } else {
            result.put("error", "CheckoutPaymentRequestForm not found for ID: " + orderId);
        }

        System.out.println("result: " + result);
        return result;
    }

    public Map<String, Object> sendPaymentRequest(Map<String, Object> result) {
        ObjectMapper mapper = new ObjectMapper();
        String nonce = UUID.randomUUID().toString();
        String requestUri = "/v3/payments/request";
        String deviceType = "PC";

        try {
            String body = mapper.writeValueAsString(result);
            System.out.println("body: " + body);
            System.out.println("nonce: " + nonce);
            String signature = encrypt(ChannelSecret,
                    ChannelSecret + requestUri + body + nonce);
            System.out.println("signature: " + signature);

            // Prepare the request body and headers
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.add("X-LINE-ChannelId", ChannelId); 
            headers.add("X-LINE-Authorization-Nonce", nonce);
            headers.add("X-LINE-Authorization", signature);
            headers.add("X-LINE-MerchantDeviceType", deviceType);

            // Create the HttpEntity
            HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(result, headers);

            // Send the POST request
            String url = "https://sandbox-api-pay.line.me/v3/payments/request";
            ResponseEntity<String> response = restTemplate.postForEntity(url, requestEntity, String.class);

            // Handle the response
            if (response.getStatusCode().is2xxSuccessful()) {
                return Map.of("status", "success", "response", response.getBody());
            } else {
                return Map.of("status", "failed", "error", "Failed to send request");
            }

        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return Map.of("status", "failed", "error", "JSON Processing Error");
        } catch (RestClientException e) {
            e.printStackTrace();
            return Map.of("status", "failed", "error", e.getMessage());
        }
    }

    // Finalizes a LINE Pay payment. LINE Pay only authorizes the amount when the
    // user approves it on their hosted page; the merchant must call this Confirm
    // API afterward (within a time limit) to actually capture it. This is what
    // was missing: the checkout flow reached "approved" but never confirmed, so
    // no payment was ever captured and no order was ever marked paid.
    public boolean confirmPayment(String transactionId, String orderId) {
        Optional<LinePayCheckoutPaymentRequestForm> optionalForm = repository.findByOrderId(orderId);
        if (optionalForm.isEmpty()) {
            System.out.println("LinePay confirm: no saved checkout request for orderId " + orderId);
            return false;
        }
        LinePayCheckoutPaymentRequestForm form = optionalForm.get();

        ObjectMapper mapper = new ObjectMapper();
        String nonce = UUID.randomUUID().toString();
        String requestUri = "/v3/payments/" + transactionId + "/confirm";

        try {
            Map<String, Object> confirmBody = Map.of(
                    "amount", form.getAmount(),
                    "currency", form.getCurrency());
            String body = mapper.writeValueAsString(confirmBody);
            String signature = encrypt(ChannelSecret, ChannelSecret + requestUri + body + nonce);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.add("X-LINE-ChannelId", ChannelId);
            headers.add("X-LINE-Authorization-Nonce", nonce);
            headers.add("X-LINE-Authorization", signature);

            HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(confirmBody, headers);
            String url = "https://sandbox-api-pay.line.me" + requestUri;
            ResponseEntity<String> response = restTemplate.postForEntity(url, requestEntity, String.class);

            System.out.println("LinePay confirm response: " + response.getBody());

            // LINE Pay always answers 200 OK; success is "0000" inside the JSON body
            Map<String, Object> parsed = mapper.readValue(response.getBody(), Map.class);
            return "0000".equals(String.valueOf(parsed.get("returnCode")));
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

}