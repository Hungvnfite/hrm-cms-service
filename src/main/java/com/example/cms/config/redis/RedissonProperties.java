package com.example.cms.config.redis;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

@Data
@Configuration
@Component
public class RedissonProperties {
    @Value("${redis.redisson.mode}")
    private String mode;
    @Value("${redis.redisson.address-single}")
    private String addressSingle;
    @Value("${redis.redisson.connection-pool-size}")
    private Integer connectionPoolSize;
    @Value("${redis.redisson.connection-minimum-idle-size}")
    private Integer connectionMinimumIdleSize;
    @Value("${redis.redisson.connect-timeout}")
    private Integer connectTimeout;
}
