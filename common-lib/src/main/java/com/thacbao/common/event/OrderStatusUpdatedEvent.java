package com.thacbao.common.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderStatusUpdatedEvent implements Serializable {
    private String orderNumber;
    private Integer userId;
    private String userEmail;
    private String userFullName;
    private String oldStatus;
    private String newStatus;
}
