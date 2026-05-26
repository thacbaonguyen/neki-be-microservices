package com.thacbao.paymentservice.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.thacbao.paymentservice.dto.request.CreatePaymentRequest;
import com.thacbao.paymentservice.service.PaymentLinkService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import vn.payos.PayOS;
import vn.payos.model.v2.paymentRequests.CreatePaymentLinkRequest;
import vn.payos.model.v2.paymentRequests.CreatePaymentLinkResponse;
import vn.payos.model.v2.paymentRequests.PaymentLinkItem;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentLinkServiceImpl implements PaymentLinkService {

    private final PayOS payOS;

    @Value("${URL.returnUrl}")
    private String returnUrl;

    @Value("${URL.cancelUrl}")
    private String cancelUrl;

    @Override
    public ObjectNode createPaymentLink(CreatePaymentRequest request) {
        ObjectMapper objectMapper = new ObjectMapper();
        ObjectNode response = objectMapper.createObjectNode();
        try {
            List<PaymentLinkItem> paymentLinkItems = request.getItems() != null
                    ? request.getItems().stream()
                            .map(item -> PaymentLinkItem.builder()
                                    .name(item.getName())
                                    .price(item.getPrice())
                                    .quantity(item.getQuantity())
                                    .build())
                            .collect(Collectors.toList())
                    : Collections.emptyList();

            long totalAmount = request.getAmount().longValue();

            CreatePaymentLinkRequest paymentLinkRequest = CreatePaymentLinkRequest.builder()
                    .orderCode(Long.parseLong(request.getOrderNumber()))
                    .amount(totalAmount)
                    .description("Order :" + request.getOrderNumber())
                    .items(paymentLinkItems)
                    .buyerPhone(request.getPhoneDelivery())
                    .buyerAddress(request.getShippingAddress())
                    .returnUrl(returnUrl)
                    .cancelUrl(cancelUrl)
                    .expiredAt(System.currentTimeMillis() / 1000 + 600)
                    .build();

            CreatePaymentLinkResponse paymentLinkResponse = payOS.paymentRequests().create(paymentLinkRequest);
            response.put("error", 0);
            response.put("message", "success");
            response.set("data", objectMapper.valueToTree(paymentLinkResponse));
            return response;
        } catch (Exception e) {
            log.error("Failed to create PayOS payment link for order {}: {}", request.getOrderNumber(), e.getMessage());
            response.put("error", -1);
            response.put("message", e.getMessage());
            response.set("data", null);
            return response;
        }
    }
}
