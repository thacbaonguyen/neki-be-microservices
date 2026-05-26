package com.thacbao.common.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserDTO {
    private Integer id;
    private String email;
    private String fullName;
    private String phoneNumber;
    private String address;
    private String avatar;
    private Boolean isActive;
    private List<String> roles;
}
