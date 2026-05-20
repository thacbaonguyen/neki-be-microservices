package com.thacbao.paymentservice.service.impl;

import com.thacbao.common.exception.AlreadyException;
import com.thacbao.common.exception.NotFoundException;
import com.thacbao.paymentservice.dto.request.PaymentMethodRequest;
import com.thacbao.paymentservice.dto.response.PaymentMethodResponse;
import com.thacbao.paymentservice.model.PaymentMethod;
import com.thacbao.paymentservice.repository.PaymentMethodRepository;
import com.thacbao.paymentservice.service.PaymentMethodService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class PaymentMethodServiceImpl implements PaymentMethodService {

    private final PaymentMethodRepository paymentMethodRepository;

    @Override
    public void create(PaymentMethodRequest request) {
        Optional<PaymentMethod> existing = paymentMethodRepository.findByName(request.getName());
        if (existing.isPresent()) {
            throw new AlreadyException("Payment method đã tồn tại: " + request.getName());
        }
        PaymentMethod paymentMethod = PaymentMethod.builder()
                .name(request.getName())
                .description(request.getDescription())
                .isActive(true)
                .build();
        paymentMethodRepository.save(paymentMethod);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PaymentMethodResponse> getAll() {
        return paymentMethodRepository.findAll().stream()
                .map(PaymentMethodResponse::from)
                .collect(Collectors.toList());
    }

    @Override
    public void update(Integer id, boolean status) {
        PaymentMethod method = paymentMethodRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Payment method not found: " + id));
        method.setIsActive(status);
        paymentMethodRepository.save(method);
    }

    @Override
    public void delete(Integer id) {
        PaymentMethod method = paymentMethodRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Payment method not found: " + id));
        paymentMethodRepository.delete(method);
    }
}
