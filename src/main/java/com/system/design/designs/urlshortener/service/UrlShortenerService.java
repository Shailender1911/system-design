package com.system.design.designs.urlshortener.service;

import com.system.design.designs.urlshortener.dto.ShortenUrlRequest;
import com.system.design.designs.urlshortener.dto.ShortenUrlResponse;
import com.system.design.designs.urlshortener.dto.UrlAnalyticsResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

/**
 * Main service interface for URL shortener operations
 */
public interface UrlShortenerService {
    
    /**
     * Create a shortened URL
     * @param request the shorten URL request
     * @return the shortened URL response
     */
    ShortenUrlResponse shortenUrl(ShortenUrlRequest request);
    
    /**
     * Get the original URL for redirection
     * @param shortUrl the short URL
     * @return the original URL
     */
    Optional<String> getOriginalUrl(String shortUrl);
    
    /**
     * Get URL details by short URL
     * @param shortUrl the short URL
     * @return the URL details
     */
    Optional<ShortenUrlResponse> getUrlDetails(String shortUrl);
    
    /**
     * Get URLs created by a user
     * @param userId the user ID
     * @param pageable pagination parameters
     * @return page of URLs
     */
    Page<ShortenUrlResponse> getUserUrls(String userId, Pageable pageable);
    
    /**
     * Get analytics for a short URL
     * @param shortUrl the short URL
     * @return analytics data
     */
    UrlAnalyticsResponse getUrlAnalytics(String shortUrl);
    
    /**
     * Update URL (activate/deactivate)
     * @param shortUrl the short URL
     * @param isActive the active status
     * @return updated URL response
     */
    ShortenUrlResponse updateUrlStatus(String shortUrl, boolean isActive);
    
    /**
     * Delete/deactivate a URL
     * @param shortUrl the short URL
     * @param userId the user ID (for authorization)
     * @return true if successful
     */
    boolean deleteUrl(String shortUrl, String userId);
    
    /**
     * Check if a custom alias is available
     * @param alias the custom alias
     * @return true if available
     */
    boolean isAliasAvailable(String alias);
    
    /**
     * Cleanup expired URLs
     * @return number of URLs cleaned up
     */
    int cleanupExpiredUrls();
} 