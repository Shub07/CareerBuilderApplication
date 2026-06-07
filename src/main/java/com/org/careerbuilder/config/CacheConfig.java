package com.org.careerbuilder.config;

import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Lightweight in-process cache for hot, read-heavy aggregates (fee stat cards).
 * Entries are evicted on every money movement; a scheduled sweep clears the rest
 * to bound staleness from async/external writes. Swap in Redis for multi-instance
 * deployments.
 */
@Configuration
@EnableCaching
public class CacheConfig {

    public static final String FEE_STATS_CACHE = "feeStats";

    @Bean
    public CacheManager cacheManager() {
        return new ConcurrentMapCacheManager(FEE_STATS_CACHE);
    }
}
