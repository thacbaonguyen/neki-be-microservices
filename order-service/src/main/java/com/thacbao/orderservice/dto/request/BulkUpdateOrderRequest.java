package com.thacbao.orderservice.dto.request;

import lombok.Data;

import java.util.List;

@Data
public class BulkUpdateOrderRequest {
    private List<Integer> orderIds;
    private String status;
}
