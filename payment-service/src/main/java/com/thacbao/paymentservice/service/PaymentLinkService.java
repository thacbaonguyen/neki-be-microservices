package com.thacbao.paymentservice.service;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.thacbao.paymentservice.dto.request.CreatePaymentRequest;

public interface PaymentLinkService {
    ObjectNode createPaymentLink(CreatePaymentRequest request);
}
