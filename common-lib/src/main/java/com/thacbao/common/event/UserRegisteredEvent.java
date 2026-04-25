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
public class UserRegisteredEvent implements Serializable {
    private Integer userId;
    private String email;
    private String fullName;
    private String otpCode;
    private String type; // OTP_VERIFICATION, WELCOME
}
