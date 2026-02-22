package com.devansh.rate_limiter.filter;

import com.devansh.rate_limiter.model.AbuseEvent;
import com.devansh.rate_limiter.model.RateLimitResult;
import com.devansh.rate_limiter.service.AbuseEventProducer;
import com.devansh.rate_limiter.service.ProxyService;
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
    private final ProxyService proxyService;
    private final AbuseEventProducer abuseEventProducer;
    private final ObjectMapper objectMapper;

    protected void  doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        String key = request.getRemoteAddr();
        String path = request.getRequestURI();

        if(isAdmin(path)){
            filterChain.doFilter(request, response);
            return;
        }

        if (rateLimitService.isAbuseBlock(key)) {
            System.out.println("ABUSE BLOCKED FOR IP: " + key);
            response.setStatus(403);
            response.getWriter().println("Your request has been blocked");
            return;
        }

        RateLimitResult result = rateLimitService.check(key, path);
        if (!result.isAllowed()) {
            System.out.println("Rate Limit: " + path);
            response.setStatus(429);
            response.getWriter().println("Rate Limit Exceeded");
            return;
        }


        long start = System.currentTimeMillis();

        try {
            var responseEntity = proxyService.forward(request).block();

            response.setStatus(responseEntity.getStatusCode().value());

            if (responseEntity.getBody() != null) {
                response.getOutputStream().write(responseEntity.getBody());
            }
        }
        finally {
            if(isAdmin(path)){
                return;
            }
            System.out.println("HERE IN KAFKA BLOCK");
            abuseEventProducer.publish("abuse-events", key, buildEventJson(request, response, start));
        }
    }

    private boolean isAdmin(String path){
        return path.contains("/ratelimiter");
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
