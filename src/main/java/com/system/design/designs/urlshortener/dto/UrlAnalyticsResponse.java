package com.system.design.designs.urlshortener.dto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public record UrlAnalyticsResponse(
    String shortUrl,
    String originalUrl,
    Long totalClicks,
    Long uniqueClicks,
    LocalDateTime firstClickAt,
    LocalDateTime lastClickAt,
    Map<String, Long> clicksByCountry,
    Map<String, Long> clicksByDevice,
    Map<String, Long> clicksByBrowser,
    Map<String, Long> clicksByReferrer,
    Map<String, Long> clicksByDate,
    List<RecentClick> recentClicks,
    Double clickThroughRate,
    LocalDateTime createdAt
) {
    
    public record RecentClick(
        String ipAddress,
        String userAgent,
        String referrer,
        String country,
        String city,
        String deviceType,
        String browser,
        String operatingSystem,
        Boolean isMobile,
        Boolean isBot,
        LocalDateTime clickedAt
    ) {}
    
    public static UrlAnalyticsResponse from(String shortUrl, String originalUrl, 
                                           Long totalClicks, Long uniqueClicks,
                                           LocalDateTime firstClickAt, LocalDateTime lastClickAt,
                                           Map<String, Long> clicksByCountry,
                                           Map<String, Long> clicksByDevice,
                                           Map<String, Long> clicksByBrowser,
                                           Map<String, Long> clicksByReferrer,
                                           Map<String, Long> clicksByDate,
                                           List<RecentClick> recentClicks,
                                           Double clickThroughRate,
                                           LocalDateTime createdAt) {
        return new UrlAnalyticsResponse(
            shortUrl,
            originalUrl,
            totalClicks,
            uniqueClicks,
            firstClickAt,
            lastClickAt,
            clicksByCountry,
            clicksByDevice,
            clicksByBrowser,
            clicksByReferrer,
            clicksByDate,
            recentClicks,
            clickThroughRate,
            createdAt
        );
    }
} 