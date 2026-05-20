package com.thacbao.paymentservice.service;

import com.thacbao.paymentservice.dto.request.PaymentMethodRequest;
import com.thacbao.paymentservice.dto.response.PaymentMethodResponse;

import java.util.List;

public interface PaymentMethodService {
    void create(PaymentMethodRequest request);

    List<PaymentMethodResponse> getAll();

    void update(Integer id, boolean status);

    void delete(Integer id);
}
