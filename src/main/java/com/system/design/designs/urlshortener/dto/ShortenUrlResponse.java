package com.system.design.designs.urlshortener.dto;

import java.time.LocalDateTime;

public record ShortenUrlResponse(
    String shortUrl,
    String originalUrl,
    String customAlias,
    String userId,
    LocalDateTime expirationDate,
    String description,
    Long clickCount,
    Boolean isActive,
    LocalDateTime createdAt
) {
    
    public static ShortenUrlResponse from(String shortUrl, String originalUrl, 
                                         String customAlias, String userId, 
                                         LocalDateTime expirationDate, String description,
                                         Long clickCount, Boolean isActive, 
                                         LocalDateTime createdAt) {
        return new ShortenUrlResponse(
            shortUrl,
            originalUrl,
            customAlias,
            userId,
            expirationDate,
            description,
            clickCount,
            isActive,
            createdAt
        );
    }
} 