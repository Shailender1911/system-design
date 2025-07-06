package com.system.design.designs.urlshortener.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

public record ShortenUrlRequest(
    @NotBlank(message = "Original URL is required")
    @Size(max = 2048, message = "URL must be less than 2048 characters")
    @Pattern(regexp = "^https?://.*", message = "URL must start with http:// or https://")
    String originalUrl,
    
    @Size(max = 50, message = "Custom alias must be less than 50 characters")
    @Pattern(regexp = "^[a-zA-Z0-9-_]*$", message = "Custom alias can only contain letters, numbers, hyphens, and underscores")
    String customAlias,
    
    String userId,
    
    LocalDateTime expirationDate,
    
    @Size(max = 500, message = "Description must be less than 500 characters")
    String description
) {
    
    public ShortenUrlRequest {
        // Compact constructor validation
        if (originalUrl != null) {
            originalUrl = originalUrl.trim();
        }
        if (customAlias != null) {
            customAlias = customAlias.trim();
            if (customAlias.isEmpty()) {
                customAlias = null;
            }
        }
        if (description != null) {
            description = description.trim();
            if (description.isEmpty()) {
                description = null;
            }
        }
        
        // Validate expiration date is in the future
        if (expirationDate != null && expirationDate.isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("Expiration date must be in the future");
        }
    }
    
    // Convenience constructors
    public ShortenUrlRequest(String originalUrl) {
        this(originalUrl, null, null, null, null);
    }
    
    public ShortenUrlRequest(String originalUrl, String customAlias) {
        this(originalUrl, customAlias, null, null, null);
    }
    
    public ShortenUrlRequest(String originalUrl, String customAlias, String userId) {
        this(originalUrl, customAlias, userId, null, null);
    }
} 