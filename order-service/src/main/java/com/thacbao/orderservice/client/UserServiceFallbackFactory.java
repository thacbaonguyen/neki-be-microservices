package com.thacbao.orderservice.client;

import com.thacbao.common.dto.UserDTO;
import com.thacbao.common.dto.response.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class UserServiceFallbackFactory implements FallbackFactory<UserServiceClient> {

    @Override
    public UserServiceClient create(Throwable cause) {
        log.error("User Service unavailable: {}", cause.getMessage());
        return new UserServiceClient() {
            @Override
            public ApiResponse<UserDTO> getUserById(Integer id) {
                log.warn("Fallback: getUserById for id={}", id);
                return ApiResponse.error(503, "User Service unavailable");
            }
        };
    }
}
