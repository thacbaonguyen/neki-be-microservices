package com.thacbao.orderservice.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public interface OrderStatisticsService {
    List<Object[]> getDailyOrderCounts(LocalDate startDate, LocalDate endDate);
    List<Object[]> getDailyRevenue(LocalDate startDate, LocalDate endDate);
    List<Object[]> getTopDistrictsByOrderCount(int limit);
    BigDecimal getTotalRevenue();
    BigDecimal getRevenueByDateRange(LocalDate startDate, LocalDate endDate);
    BigDecimal getTotalRevenueByUser(Integer userId);
    long countTotalOrders();
    long countOrdersByStatus(String status);
    long countOrdersByDateRange(LocalDate startDate, LocalDate endDate);
    long countOrdersByUser(Integer userId);
    BigDecimal getAverageOrderValue();
    List<Object[]> getMonthlyStatistics(int year);
    List<Object[]> getTopSellingProducts(int limit);
    List<Object[]> getOrderStatusDistribution();
    Map<String, Object> getOverviewMetrics();
    long countValidOrders();
}
