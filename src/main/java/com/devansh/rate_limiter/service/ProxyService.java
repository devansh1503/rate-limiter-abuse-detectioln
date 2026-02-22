package com.devansh.rate_limiter.service;

import jakarta.servlet.http.HttpServletRequest;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.net.URI;

@Service
@RequiredArgsConstructor
public class ProxyService {
    private final WebClient webClient;

    private static final String BACKEND_URL = "http://host.docker.internal:8000";

    public Mono<ResponseEntity<byte[]>> forward(HttpServletRequest request) {
        return webClient
                .method(HttpMethod.valueOf(request.getMethod()))
                .uri(BACKEND_URL + request.getRequestURI())
                .retrieve()
                .toEntity(byte[].class);
    }
}
