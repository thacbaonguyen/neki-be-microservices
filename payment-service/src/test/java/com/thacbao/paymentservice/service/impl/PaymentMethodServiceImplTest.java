package com.thacbao.paymentservice.service.impl;

import com.thacbao.common.exception.AlreadyException;
import com.thacbao.common.exception.NotFoundException;
import com.thacbao.paymentservice.dto.request.PaymentMethodRequest;
import com.thacbao.paymentservice.dto.response.PaymentMethodResponse;
import com.thacbao.paymentservice.model.PaymentMethod;
import com.thacbao.paymentservice.repository.PaymentMethodRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PaymentMethodServiceImplTest {

    @Mock
    private PaymentMethodRepository paymentMethodRepository;

    @InjectMocks
    private PaymentMethodServiceImpl paymentMethodService;

    @Test
    void create_success() {
        PaymentMethodRequest request = new PaymentMethodRequest();
        request.setName("Cash");
        request.setDescription("Cash on delivery");

        when(paymentMethodRepository.findByName("Cash")).thenReturn(Optional.empty());

        paymentMethodService.create(request);

        verify(paymentMethodRepository).save(any(PaymentMethod.class));
    }

    @Test
    void create_alreadyExists_throws() {
        PaymentMethodRequest request = new PaymentMethodRequest();
        request.setName("Cash");

        when(paymentMethodRepository.findByName("Cash")).thenReturn(Optional.of(new PaymentMethod()));

        assertThrows(AlreadyException.class, () -> paymentMethodService.create(request));
    }

    @Test
    void getAll_success() {
        PaymentMethod method = PaymentMethod.builder().name("Cash").isActive(true).build();
        method.setId(1);
        when(paymentMethodRepository.findAll()).thenReturn(List.of(method));

        List<PaymentMethodResponse> result = paymentMethodService.getAll();

        assertEquals(1, result.size());
        assertEquals("Cash", result.get(0).getName());
    }

    @Test
    void update_success() {
        PaymentMethod method = PaymentMethod.builder().name("Cash").isActive(true).build();
        method.setId(1);
        when(paymentMethodRepository.findById(1)).thenReturn(Optional.of(method));

        paymentMethodService.update(1, false);

        assertFalse(method.getIsActive());
        verify(paymentMethodRepository).save(method);
    }

    @Test
    void update_notFound_throws() {
        when(paymentMethodRepository.findById(1)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> paymentMethodService.update(1, false));
    }

    @Test
    void delete_success() {
        PaymentMethod method = PaymentMethod.builder().name("Cash").isActive(true).build();
        method.setId(1);
        when(paymentMethodRepository.findById(1)).thenReturn(Optional.of(method));

        paymentMethodService.delete(1);

        verify(paymentMethodRepository).delete(method);
    }
}
