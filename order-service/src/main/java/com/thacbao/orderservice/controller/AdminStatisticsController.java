package com.thacbao.orderservice.controller;

import com.thacbao.common.dto.response.ApiResponse;
import com.thacbao.orderservice.service.OrderStatisticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/admin/statistics")
@RequiredArgsConstructor
public class AdminStatisticsController {

    private final OrderStatisticsService statisticsService;

    @GetMapping("/overview")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getOverview() {
        return ResponseEntity.ok(ApiResponse.success(statisticsService.getOverviewMetrics()));
    }

    @GetMapping("/revenue")
    public ResponseEntity<ApiResponse<BigDecimal>> getTotalRevenue() {
        return ResponseEntity.ok(ApiResponse.success(statisticsService.getTotalRevenue()));
    }

    @GetMapping("/revenue/range")
    public ResponseEntity<ApiResponse<BigDecimal>> getRevenueByRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return ResponseEntity.ok(ApiResponse.success(statisticsService.getRevenueByDateRange(startDate, endDate)));
    }

    @GetMapping("/daily-orders")
    public ResponseEntity<ApiResponse<List<Object[]>>> getDailyOrders(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return ResponseEntity.ok(ApiResponse.success(statisticsService.getDailyOrderCounts(startDate, endDate)));
    }

    @GetMapping("/daily-revenue")
    public ResponseEntity<ApiResponse<List<Object[]>>> getDailyRevenue(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return ResponseEntity.ok(ApiResponse.success(statisticsService.getDailyRevenue(startDate, endDate)));
    }

    @GetMapping("/monthly")
    public ResponseEntity<ApiResponse<List<Object[]>>> getMonthlyStats(@RequestParam int year) {
        return ResponseEntity.ok(ApiResponse.success(statisticsService.getMonthlyStatistics(year)));
    }

    @GetMapping("/top-districts")
    public ResponseEntity<ApiResponse<List<Object[]>>> getTopDistricts(
            @RequestParam(defaultValue = "10") int limit) {
        return ResponseEntity.ok(ApiResponse.success(statisticsService.getTopDistrictsByOrderCount(limit)));
    }

    @GetMapping("/top-products")
    public ResponseEntity<ApiResponse<List<Object[]>>> getTopProducts(
            @RequestParam(defaultValue = "10") int limit) {
        return ResponseEntity.ok(ApiResponse.success(statisticsService.getTopSellingProducts(limit)));
    }

    @GetMapping("/status-distribution")
    public ResponseEntity<ApiResponse<List<Object[]>>> getStatusDistribution() {
        return ResponseEntity.ok(ApiResponse.success(statisticsService.getOrderStatusDistribution()));
    }

    @GetMapping("/average-order-value")
    public ResponseEntity<ApiResponse<BigDecimal>> getAverageOrderValue() {
        return ResponseEntity.ok(ApiResponse.success(statisticsService.getAverageOrderValue()));
    }
}
