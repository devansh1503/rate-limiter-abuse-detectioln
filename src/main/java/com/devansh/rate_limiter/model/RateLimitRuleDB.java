package com.devansh.rate_limiter.model;

import com.devansh.rate_limiter.enums.Algorithm;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "ratelimitrule")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RateLimitRuleDB {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "rate_limit")
    private int limit;
    private long windowMillis;
    private String endpoint;
    private String author;
    @Enumerated(EnumType.STRING)
    private Algorithm algorithm;
}
