package com.thacbao.orderservice.client;

import com.thacbao.common.dto.UserDTO;
import com.thacbao.common.dto.response.ApiResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "user-service", fallbackFactory = UserServiceFallbackFactory.class)
public interface UserServiceClient {

    @GetMapping("/internal/users/{id}")
    ApiResponse<UserDTO> getUserById(@PathVariable Integer id);
}
