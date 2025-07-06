package com.system.design.designs.urlshortener.service;

import com.system.design.designs.urlshortener.dto.UrlAnalyticsResponse;
import jakarta.servlet.http.HttpServletRequest;

/**
 * Service interface for URL analytics operations
 */
public interface UrlAnalyticsService {
    
    /**
     * Record a click event for analytics
     * @param shortUrl the short URL
     * @param request the HTTP request containing user information
     */
    void recordClick(String shortUrl, HttpServletRequest request);
    
    /**
     * Get analytics for a short URL
     * @param shortUrl the short URL
     * @return analytics response
     */
    UrlAnalyticsResponse getUrlAnalytics(String shortUrl);
    
    /**
     * Process and store analytics data from user agent
     * @param shortUrl the short URL
     * @param ipAddress the user's IP address
     * @param userAgent the user agent string
     * @param referrer the referrer URL
     */
    void processAnalytics(String shortUrl, String ipAddress, String userAgent, String referrer);
} 