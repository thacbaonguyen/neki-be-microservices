package com.thacbao.orderservice.repository;

import com.thacbao.orderservice.dto.request.OrderFilterRequest;
import com.thacbao.orderservice.model.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public interface OrderRepositoryCustom {

    Page<Order> filterOrders(OrderFilterRequest filter, Pageable pageable);

    Page<Order> findByDateRange(LocalDate startDate, LocalDate endDate, Pageable pageable);

    Page<Order> findByDistrict(String district, Pageable pageable);

    List<Object[]> getDailyOrderCounts(LocalDate startDate, LocalDate endDate);

    List<Object[]> getDailyRevenue(LocalDate startDate, LocalDate endDate);

    BigDecimal getTotalRevenueByUser(Integer userId);

    List<Object[]> getTopDistrictsByOrderCount(int limit);

    List<Object[]> getTopSellingProducts(int limit);

    List<Object[]> getOrderStatusDistribution();
}
