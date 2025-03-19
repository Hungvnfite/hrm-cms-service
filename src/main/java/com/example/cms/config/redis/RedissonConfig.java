package com.example.cms.config.redis;

import lombok.RequiredArgsConstructor;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RedissonConfig {
    final String SINGLE_MODE = "SINGLE";
    private final RedissonProperties properties;

    @Bean
    public RedissonClient redissonClient() {
        Config config = new Config();
        if (SINGLE_MODE.equals(properties.getMode())) {
            config.useSingleServer().setAddress(properties.getAddressSingle())
                    .setConnectionPoolSize(properties.getConnectionPoolSize())
                    .setConnectionMinimumIdleSize(properties.getConnectionMinimumIdleSize())
                    .setConnectTimeout(properties.getConnectTimeout());
        }
        return Redisson.create(config);
    }
}
