package com.devansh.rate_limiter.controller;

import com.devansh.rate_limiter.service.ProxyService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@AllArgsConstructor
@NoArgsConstructor
@RequiredArgsConstructor
public class ProxyController {
    private ProxyService proxyService;

    @RequestMapping("/**")
    public Mono<ResponseEntity<byte[]>> proxy(HttpServletRequest request) {
        return proxyService.forward(request);
    }
}
