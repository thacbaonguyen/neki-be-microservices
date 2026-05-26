package com.thacbao.paymentservice.service.impl;

import com.thacbao.common.enums.PaymentStatus;
import com.thacbao.common.exception.NotFoundException;
import com.thacbao.paymentservice.dto.request.CreatePaymentRequest;
import com.thacbao.paymentservice.dto.response.PaymentResponse;
import com.thacbao.paymentservice.model.Payment;
import com.thacbao.paymentservice.model.PaymentMethod;
import com.thacbao.paymentservice.repository.PaymentMethodRepository;
import com.thacbao.paymentservice.repository.PaymentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import vn.payos.PayOS;
import vn.payos.model.v2.paymentRequests.PaymentLink;
import vn.payos.model.v2.paymentRequests.PaymentLinkStatus;
import vn.payos.service.blocking.v2.paymentRequests.PaymentRequestsService;
import vn.payos.model.webhooks.WebhookData;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentServiceImplTest {

    @Mock private PaymentRepository paymentRepository;
    @Mock private PaymentMethodRepository paymentMethodRepository;
    @Mock private PaymentEventPublisher eventPublisher;
    @Mock private PayOS payOS;

    @InjectMocks
    private PaymentServiceImpl paymentService;

    private PaymentMethod paymentMethod;
    private Payment payment;

    @BeforeEach
    void setUp() {
        paymentMethod = PaymentMethod.builder().name("PayOS").isActive(true).build();
        paymentMethod.setId(1);
        payment = Payment.builder().orderId(1).orderNumber("NEKI-001")
                .userId(1).userEmail("test@test.com").paymentMethod(paymentMethod)
                .amount(BigDecimal.valueOf(100000)).status(PaymentStatus.PENDING).build();
        payment.setId(1);
    }

    @Test
    void createPayment_success() {
        CreatePaymentRequest request = CreatePaymentRequest.builder()
                .paymentMethodId(1).orderId(1).orderNumber("NEKI-001")
                .userId(1).userEmail("test@test.com").amount(BigDecimal.valueOf(100000)).build();
        when(paymentMethodRepository.findById(1)).thenReturn(Optional.of(paymentMethod));
        when(paymentRepository.save(any(Payment.class))).thenAnswer(i -> {
            Payment p = i.getArgument(0);
            p.setId(1);
            return p;
        });

        PaymentResponse result = paymentService.createPayment(request);

        assertNotNull(result);
        verify(paymentRepository).save(any(Payment.class));
    }

    @Test
    void createPayment_methodNotFound_throws() {
        CreatePaymentRequest request = CreatePaymentRequest.builder().paymentMethodId(999).build();
        when(paymentMethodRepository.findById(999)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> paymentService.createPayment(request));
    }

    @Test
    void updateStatus_toPaid_setsPaidAt() {
        when(paymentRepository.findById(1)).thenReturn(Optional.of(payment));
        when(paymentRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        PaymentResponse result = paymentService.updateStatus(1, PaymentStatus.PAID);

        assertEquals(PaymentStatus.PAID, payment.getStatus());
        assertNotNull(payment.getPaidAt());
    }

    @Test
    void updateStatus_toFailed_noPaidAt() {
        when(paymentRepository.findById(1)).thenReturn(Optional.of(payment));
        when(paymentRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        paymentService.updateStatus(1, PaymentStatus.FAILED);

        assertEquals(PaymentStatus.FAILED, payment.getStatus());
        assertNull(payment.getPaidAt());
    }

    @Test
    void updateStatus_notFound_throws() {
        when(paymentRepository.findById(999)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> paymentService.updateStatus(999, PaymentStatus.PAID));
    }

    @Test
    void handlePayOSWebhook_success_publishesCompletedEvent() {
        WebhookData data = mock(WebhookData.class);
        when(data.getOrderCode()).thenReturn(1L);
        when(data.getCode()).thenReturn("00");
        when(data.getReference()).thenReturn("TXN123");
        when(paymentRepository.findByOrderNumber("1")).thenReturn(Optional.of(payment));
        when(paymentRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        paymentService.handlePayOSWebhook(data);

        assertEquals(PaymentStatus.PAID, payment.getStatus());
        verify(eventPublisher).publishPaymentCompleted(any());
    }

    @Test
    void handlePayOSWebhook_failed_publishesFailedEvent() {
        WebhookData data = mock(WebhookData.class);
        when(data.getOrderCode()).thenReturn(1L);
        when(data.getCode()).thenReturn("99");
        when(paymentRepository.findByOrderNumber("1")).thenReturn(Optional.of(payment));
        when(paymentRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        paymentService.handlePayOSWebhook(data);

        assertEquals(PaymentStatus.FAILED, payment.getStatus());
        verify(eventPublisher).publishPaymentFailed(any());
    }

    @Test
    void handlePayOSWebhook_paymentNotFound_throws() {
        WebhookData data = mock(WebhookData.class);
        when(data.getOrderCode()).thenReturn(999L);
        when(paymentRepository.findByOrderNumber("999")).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> paymentService.handlePayOSWebhook(data));
    }

    @Test
    void confirmPayment_success() throws Exception {
        payment.setOrderNumber("100001");
        when(paymentRepository.findByOrderNumber("100001")).thenReturn(Optional.of(payment));
        PaymentRequestsService paymentRequestApi = mock(PaymentRequestsService.class);
        PaymentLink paymentLink = mock(PaymentLink.class);
        when(paymentLink.getStatus()).thenReturn(PaymentLinkStatus.PAID);
        when(paymentRequestApi.get(100001L)).thenReturn(paymentLink);
        when(payOS.paymentRequests()).thenReturn(paymentRequestApi);
        when(paymentRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        paymentService.confirmPayment("100001");

        assertEquals(PaymentStatus.PAID, payment.getStatus());
        verify(eventPublisher).publishPaymentCompleted(any());
    }

    @Test
    void confirmPayment_alreadyPaid_skips() {
        payment.setStatus(PaymentStatus.PAID);
        when(paymentRepository.findByOrderNumber("NEKI-001")).thenReturn(Optional.of(payment));

        paymentService.confirmPayment("NEKI-001");

        verify(payOS, never()).paymentRequests();
        verify(eventPublisher, never()).publishPaymentCompleted(any());
    }

    @Test
    void confirmPayment_payOSError_throws() throws Exception {
        payment.setOrderNumber("100002");
        when(paymentRepository.findByOrderNumber("100002")).thenReturn(Optional.of(payment));
        PaymentRequestsService paymentRequestApi = mock(PaymentRequestsService.class);
        when(paymentRequestApi.get(100002L)).thenThrow(new RuntimeException("PayOS down"));
        when(payOS.paymentRequests()).thenReturn(paymentRequestApi);

        assertThrows(RuntimeException.class, () -> paymentService.confirmPayment("100002"));
    }

    @Test
    void getByOrderNumber_success() {
        when(paymentRepository.findByOrderNumber("NEKI-001")).thenReturn(Optional.of(payment));

        PaymentResponse result = paymentService.getByOrderNumber("NEKI-001");

        assertNotNull(result);
    }

    @Test
    void getByOrderNumber_notFound_throws() {
        when(paymentRepository.findByOrderNumber("INVALID")).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> paymentService.getByOrderNumber("INVALID"));
    }
}
