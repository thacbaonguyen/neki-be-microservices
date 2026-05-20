package com.thacbao.paymentservice.service.impl;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.thacbao.paymentservice.dto.request.CreatePaymentRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import vn.payos.PayOS;
import vn.payos.model.v2.paymentRequests.CreatePaymentLinkResponse;
import vn.payos.service.blocking.v2.paymentRequests.PaymentRequestsService;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PaymentLinkServiceImplTest {

    @Mock
    private PayOS payOS;

    @InjectMocks
    private PaymentLinkServiceImpl paymentLinkService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(paymentLinkService, "returnUrl", "http://localhost:3000/return");
        ReflectionTestUtils.setField(paymentLinkService, "cancelUrl", "http://localhost:3000/cancel");
    }

    @Test
    void createPaymentLink_success() throws Exception {
        CreatePaymentRequest request = new CreatePaymentRequest();
        request.setOrderNumber("12345");
        request.setAmount(BigDecimal.valueOf(100000));
        request.setPhoneDelivery("0123456789");
        request.setShippingAddress("Test Address");
        
        CreatePaymentRequest.PaymentItemInfo item = new CreatePaymentRequest.PaymentItemInfo();
        item.setName("Item 1");
        item.setPrice(50000);
        item.setQuantity(2);
        request.setItems(List.of(item));

        PaymentRequestsService paymentRequestApi = mock(PaymentRequestsService.class);
        CreatePaymentLinkResponse linkResponse = mock(CreatePaymentLinkResponse.class);
        
        when(payOS.paymentRequests()).thenReturn(paymentRequestApi);
        when(paymentRequestApi.create(any())).thenReturn(linkResponse);

        ObjectNode response = paymentLinkService.createPaymentLink(request);

        assertNotNull(response);
        assertEquals(0, response.get("error").asInt());
    }

    @Test
    void createPaymentLink_exception() throws Exception {
        CreatePaymentRequest request = new CreatePaymentRequest();
        request.setOrderNumber("12345");
        request.setAmount(BigDecimal.valueOf(100000));

        PaymentRequestsService paymentRequestApi = mock(PaymentRequestsService.class);
        when(payOS.paymentRequests()).thenReturn(paymentRequestApi);
        when(paymentRequestApi.create(any())).thenThrow(new RuntimeException("API Error"));

        ObjectNode response = paymentLinkService.createPaymentLink(request);

        assertNotNull(response);
        assertEquals(-1, response.get("error").asInt());
    }
}
