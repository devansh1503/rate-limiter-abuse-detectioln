package com.devansh.rate_limiter.repository;

import com.devansh.rate_limiter.model.RateLimitRuleDB;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RateLimitRuleRepo extends JpaRepository<RateLimitRuleDB, Long> {
}
