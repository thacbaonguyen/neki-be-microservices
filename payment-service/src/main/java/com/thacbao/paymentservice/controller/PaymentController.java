package com.thacbao.paymentservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.thacbao.paymentservice.service.PaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import vn.payos.PayOS;
import vn.payos.model.webhooks.Webhook;
import vn.payos.model.webhooks.WebhookData;

@RestController
@RequestMapping("/payment")
@RequiredArgsConstructor
@Slf4j
public class PaymentController {

    private final PayOS payOS;
    private final PaymentService paymentService;

    @PostMapping("/payos_transfer_handler")
    public ObjectNode payosTransferHandler(@RequestBody ObjectNode body) {
        log.info("PayOS transfer handler received");
        ObjectMapper objectMapper = new ObjectMapper();
        ObjectNode response = objectMapper.createObjectNode();

        try {
            Webhook webhookBody = objectMapper.treeToValue(body, Webhook.class);
            response.put("error", 0);
            response.put("message", "Webhook delivered");
            response.set("data", null);

            WebhookData data = payOS.webhooks().verify(webhookBody);
            paymentService.handlePayOSWebhook(data);
            return response;
        } catch (Exception e) {
            log.error("PayOS webhook error: {}", e.getMessage());
            response.put("error", -1);
            response.put("message", e.getMessage());
            response.set("data", null);
            return response;
        }
    }

    @PostMapping("/confirm")
    public ObjectNode confirmPayment(@RequestBody ObjectNode body) {
        ObjectMapper objectMapper = new ObjectMapper();
        ObjectNode response = objectMapper.createObjectNode();
        try {
            String orderCode = body.get("orderCode").asText();
            paymentService.confirmPayment(orderCode);
            response.put("error", 0);
            response.put("message", "Payment confirmed");
            response.set("data", null);
            return response;
        } catch (Exception e) {
            log.error("Confirm payment error: {}", e.getMessage());
            response.put("error", -1);
            response.put("message", e.getMessage());
            response.set("data", null);
            return response;
        }
    }
}
