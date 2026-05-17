package com.thacbao.orderservice.service.impl;

import com.thacbao.common.enums.OrderStatus;
import com.thacbao.orderservice.repository.OrderRepository;
import com.thacbao.orderservice.service.OrderStatisticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OrderStatisticsServiceImpl implements OrderStatisticsService {

    private final OrderRepository orderRepository;

    @Override
    public List<Object[]> getDailyOrderCounts(LocalDate startDate, LocalDate endDate) {
        return orderRepository.getDailyOrderCounts(startDate, endDate);
    }

    @Override
    public List<Object[]> getDailyRevenue(LocalDate startDate, LocalDate endDate) {
        return orderRepository.getDailyRevenue(startDate, endDate);
    }

    @Override
    public List<Object[]> getTopDistrictsByOrderCount(int limit) {
        return orderRepository.getTopDistrictsByOrderCount(limit);
    }

    @Override
    public BigDecimal getTotalRevenue() {
        return orderRepository.getTotalRevenue();
    }

    @Override
    public BigDecimal getRevenueByDateRange(LocalDate startDate, LocalDate endDate) {
        return orderRepository.getRevenueByDateRange(
                startDate.atStartOfDay(), endDate.atTime(LocalTime.MAX));
    }

    @Override
    public BigDecimal getTotalRevenueByUser(Integer userId) {
        return orderRepository.getTotalRevenueByUser(userId);
    }

    @Override
    public long countTotalOrders() {
        return orderRepository.count();
    }

    @Override
    public long countOrdersByStatus(String status) {
        return orderRepository.countByStatus(OrderStatus.valueOf(status.toUpperCase()));
    }

    @Override
    public long countOrdersByDateRange(LocalDate startDate, LocalDate endDate) {
        return orderRepository.countByDateRange(
                startDate.atStartOfDay(), endDate.atTime(LocalTime.MAX));
    }

    @Override
    public long countOrdersByUser(Integer userId) {
        return orderRepository.countByUserId(userId);
    }

    @Override
    public BigDecimal getAverageOrderValue() {
        return orderRepository.getAverageOrderValue();
    }

    @Override
    public List<Object[]> getMonthlyStatistics(int year) {
        return orderRepository.getMonthlyStatistics(year);
    }

    @Override
    public List<Object[]> getTopSellingProducts(int limit) {
        return orderRepository.getTopSellingProducts(limit);
    }

    @Override
    public List<Object[]> getOrderStatusDistribution() {
        return orderRepository.getOrderStatusDistribution();
    }

    @Override
    public Map<String, Object> getOverviewMetrics() {
        Map<String, Object> metrics = new HashMap<>();
        metrics.put("totalOrders", countTotalOrders());
        metrics.put("validOrders", countValidOrders());
        metrics.put("totalRevenue", getTotalRevenue());
        metrics.put("avgOrderValue", getAverageOrderValue());
        
        List<Object[]> todayRev = getDailyRevenue(LocalDate.now(), LocalDate.now());
        BigDecimal todayRevenue = todayRev.isEmpty() ? BigDecimal.ZERO : (BigDecimal) todayRev.get(0)[1];
        metrics.put("todayRevenue", todayRevenue);
        
        metrics.put("pendingOrders", countOrdersByStatus("PENDING"));
        metrics.put("processingOrders", countOrdersByStatus("CONFIRMED"));
        metrics.put("shippingOrders", countOrdersByStatus("SHIPPED"));
        metrics.put("deliveredOrders", countOrdersByStatus("DELIVERED"));
        metrics.put("cancelledOrders", countOrdersByStatus("CANCELLED"));
        return metrics;
    }

    @Override
    public long countValidOrders() {
        return orderRepository.countValidOrders();
    }
}
