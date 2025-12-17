package com.devansh.rate_limiter.filter;

import com.devansh.rate_limiter.model.RateLimitResult;
import com.devansh.rate_limiter.service.RateLimitService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@AllArgsConstructor
@RequiredArgsConstructor

public class RateLimitFilter extends OncePerRequestFilter {

    private final RateLimitService rateLimitService;

    protected void  doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        String key = request.getRemoteAddr();
        String path = request.getRequestURI();

        RateLimitResult result = rateLimitService.check(key, path);
        if(!result.isAllowed()) {
            response.setStatus(429);
            response.getWriter().println("Rate Limit Exceeded");
            return;
        }

        filterChain.doFilter(request, response);
    }
}
