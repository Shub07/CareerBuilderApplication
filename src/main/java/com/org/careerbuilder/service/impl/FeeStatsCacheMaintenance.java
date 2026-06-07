package com.org.careerbuilder.service.impl;

import com.org.careerbuilder.config.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Bounds staleness of the fee-stats cache caused by async obligation generation
 * or external writes (the synchronous collect/refund paths evict on their own).
 */
@Component
public class FeeStatsCacheMaintenance {

    @Scheduled(fixedDelayString = "${fee.stats.cache-sweep-ms:300000}")
    @CacheEvict(cacheNames = CacheConfig.FEE_STATS_CACHE, allEntries = true)
    public void sweep() {
        // Annotation-driven full eviction; method body intentionally empty.
    }
}
