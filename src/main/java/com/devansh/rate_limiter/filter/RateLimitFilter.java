package com.devansh.rate_limiter.filter;

import com.devansh.rate_limiter.model.AbuseEvent;
import com.devansh.rate_limiter.model.RateLimitResult;
import com.devansh.rate_limiter.service.AbuseEventProducer;
import com.devansh.rate_limiter.service.RateLimitService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;

@Component
@RequiredArgsConstructor

public class RateLimitFilter extends OncePerRequestFilter {

    private final RateLimitService rateLimitService;
    private final AbuseEventProducer abuseEventProducer;
    private final ObjectMapper objectMapper;

    protected void  doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        String key = request.getRemoteAddr();
        String path = request.getRequestURI();

        if(rateLimitService.isAbuseBlock(key)){
            response.setStatus(429);
            response.getWriter().println("Your request has been blocked");
            return;
        }

        RateLimitResult result = rateLimitService.check(key, path);
        if(!result.isAllowed()) {
            response.setStatus(429);
            response.getWriter().println("Rate Limit Exceeded");
            return;
        }

        long start = System.currentTimeMillis();

        try{
            filterChain.doFilter(request, response);
        }
        finally {
            abuseEventProducer.publish("abuse-event", key, buildEventJson(request, response, start));
        }
    }

    private String buildEventJson(HttpServletRequest request, HttpServletResponse response, long start) {
        AbuseEvent event = AbuseEvent.builder()
                .ip(request.getRemoteAddr())
                .method(request.getMethod())
                .path(request.getRequestURI())
                .status(response.getStatus())
                .latency(System.currentTimeMillis() - start)
                .timestamp(System.currentTimeMillis())
                .build();

        return objectMapper.writeValueAsString(event);
    }
}
