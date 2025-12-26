package com.devansh.rate_limiter.controller;

import com.devansh.rate_limiter.model.RateLimitRuleRequest;
import com.devansh.rate_limiter.service.RuleService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/ratelimiter")
@RequiredArgsConstructor
public class RulesController {
    private final RuleService ruleService;

    @PostMapping("/create-rule")
    public ResponseEntity<?> createRule(@RequestBody RateLimitRuleRequest rateLimitRuleRequest) {
        return ruleService.createRule(rateLimitRuleRequest);
    }

    @PostMapping("/update-rule/{id}")
    public ResponseEntity<?> updateRule(@RequestBody RateLimitRuleRequest rateLimitRuleRequest, @PathVariable String id) {
        return ruleService.updateRule(Long.parseLong(id), rateLimitRuleRequest);
    }
}
