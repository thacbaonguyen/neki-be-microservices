package com.thacbao.orderservice.service.impl;

import com.thacbao.common.enums.DiscountType;
import com.thacbao.common.exception.NotFoundException;
import com.thacbao.orderservice.dto.request.DiscountRequest;
import com.thacbao.orderservice.dto.response.DiscountResponse;
import com.thacbao.orderservice.model.Discount;
import com.thacbao.orderservice.model.DiscountUsage;
import com.thacbao.orderservice.model.Order;
import com.thacbao.orderservice.repository.DiscountRepository;
import com.thacbao.orderservice.repository.DiscountUsageRepository;
import com.thacbao.orderservice.service.DiscountService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class DiscountServiceImpl implements DiscountService {

    private final DiscountRepository discountRepository;
    private final DiscountUsageRepository discountUsageRepository;

    @Override
    public void create(DiscountRequest request) {
        Discount discount = Discount.builder()
                .name(request.getName())
                .code(request.getCode())
                .percent(request.getPercent())
                .reduceAmount(request.getReduceAmount())
                .discountType(DiscountType.valueOf(request.getDiscountType().toUpperCase()))
                .description(request.getDescription())
                .isActive(request.isActive())
                .usageLimit(request.getUsageLimit())
                .userUsageLimit(request.getUserUsageLimit())
                .minOrderAmount(request.getMinOrderAmount())
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .build();
        discountRepository.save(discount);
    }

    @Override
    public void update(Integer id, DiscountRequest request) {
        Discount discount = discountRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Discount not found: " + id));
        discount.setName(request.getName());
        discount.setCode(request.getCode());
        discount.setPercent(request.getPercent());
        discount.setReduceAmount(request.getReduceAmount());
        discount.setDiscountType(DiscountType.valueOf(request.getDiscountType().toUpperCase()));
        discount.setDescription(request.getDescription());
        discount.setActive(request.isActive());
        discount.setUsageLimit(request.getUsageLimit());
        discount.setUserUsageLimit(request.getUserUsageLimit());
        discount.setMinOrderAmount(request.getMinOrderAmount());
        discount.setStartDate(request.getStartDate());
        discount.setEndDate(request.getEndDate());
        discountRepository.save(discount);
    }

    @Override
    public void delete(Integer id) {
        discountRepository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<DiscountResponse> getAllByType(String discountType) {
        DiscountType type = DiscountType.valueOf(discountType.toUpperCase());
        return discountRepository.findByDiscountType(type).stream()
                .map(DiscountResponse::from)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Discount validateAndGetDiscount(String code, Integer userId, BigDecimal orderAmount) {
        Discount discount = discountRepository.findByCode(code)
                .orElseThrow(() -> new NotFoundException("Discount not found: " + code));

        if (!discount.isActive()) {
            throw new IllegalStateException("Discount is not active");
        }

        if (discount.getStartDate() != null && LocalDate.now().isBefore(discount.getStartDate())) {
            throw new IllegalStateException("Discount not yet started");
        }

        if (discount.getEndDate() != null && LocalDate.now().isAfter(discount.getEndDate())) {
            throw new IllegalStateException("Discount expired");
        }

        if (discount.getUsageLimit() != null && discount.getUsedCount() >= discount.getUsageLimit()) {
            throw new IllegalStateException("Discount usage limit reached");
        }

        if (discount.getUserUsageLimit() != null) {
            long userUsageCount = discountUsageRepository.countByUserIdAndDiscount(userId, discount);
            if (userUsageCount >= discount.getUserUsageLimit()) {
                throw new IllegalStateException("User discount usage limit reached");
            }
        }

        if (discount.getMinOrderAmount() != null && orderAmount.compareTo(discount.getMinOrderAmount()) < 0) {
            throw new IllegalStateException("Order amount below minimum for discount");
        }

        return discount;
    }

    @Override
    public void recordUsage(Discount discount, Integer userId, Order order) {
        DiscountUsage usage = DiscountUsage.builder()
                .userId(userId)
                .discount(discount)
                .order(order)
                .build();
        discountUsageRepository.save(usage);

        discount.setUsedCount(discount.getUsedCount() + 1);
        discountRepository.save(discount);
    }
}
