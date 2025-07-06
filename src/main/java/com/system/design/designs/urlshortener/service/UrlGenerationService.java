package com.system.design.designs.urlshortener.service;

/**
 * Service interface for URL generation strategies
 */
public interface UrlGenerationService {
    
    /**
     * Generate a short URL using hash-based approach
     * @param originalUrl the original URL
     * @return generated short URL
     */
    String generateShortUrl(String originalUrl);
    
    /**
     * Generate a short URL using counter-based approach
     * @return generated short URL
     */
    String generateShortUrlWithCounter();
    
    /**
     * Generate a short URL with custom length
     * @param originalUrl the original URL
     * @param length the desired length
     * @return generated short URL
     */
    String generateShortUrl(String originalUrl, int length);
    
    /**
     * Validate if a custom alias is available
     * @param alias the custom alias
     * @return true if available, false otherwise
     */
    boolean isAliasAvailable(String alias);
    
    /**
     * Check if a short URL already exists
     * @param shortUrl the short URL to check
     * @return true if exists, false otherwise
     */
    boolean shortUrlExists(String shortUrl);
} 