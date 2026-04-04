package com.thacbao.userservice.dto.request;

import lombok.Data;

@Data
public class UserFilterRequest {
    private String keyword;
    private String provider;
    private Boolean isActive;
}
