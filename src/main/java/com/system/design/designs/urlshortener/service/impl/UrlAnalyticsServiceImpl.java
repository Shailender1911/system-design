package com.system.design.designs.urlshortener.service.impl;

import com.system.design.designs.urlshortener.dto.UrlAnalyticsResponse;
import com.system.design.designs.urlshortener.entity.UrlAnalytics;
import com.system.design.designs.urlshortener.entity.UrlMapping;
import com.system.design.designs.urlshortener.repository.UrlAnalyticsRepository;
import com.system.design.designs.urlshortener.repository.UrlMappingRepository;
import com.system.design.designs.urlshortener.service.UrlAnalyticsService;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class UrlAnalyticsServiceImpl implements UrlAnalyticsService {
    
    private static final Logger logger = LoggerFactory.getLogger(UrlAnalyticsServiceImpl.class);
    
    @Autowired
    private UrlAnalyticsRepository urlAnalyticsRepository;
    
    @Autowired
    private UrlMappingRepository urlMappingRepository;
    
    @Override
    public void recordClick(String shortUrl, HttpServletRequest request) {
        logger.debug("Recording click for short URL: {}", shortUrl);
        
        try {
            String ipAddress = getClientIpAddress(request);
            String userAgent = request.getHeader("User-Agent");
            String referrer = request.getHeader("Referer");
            
            processAnalytics(shortUrl, ipAddress, userAgent, referrer);
            
        } catch (Exception e) {
            logger.error("Failed to record click for URL: {}", shortUrl, e);
            // Don't fail the request if analytics fails
        }
    }
    
    @Override
    public void processAnalytics(String shortUrl, String ipAddress, String userAgent, String referrer) {
        logger.debug("Processing analytics for short URL: {}", shortUrl);
        
        try {
            // Parse user agent
            UserAgentInfo userAgentInfo = parseUserAgent(userAgent);
            
            // Get geographic info (in a real system, you'd use a service like GeoIP)
            GeoInfo geoInfo = getGeoInfo(ipAddress);
            
            // Create analytics record
            UrlAnalytics analytics = new UrlAnalytics(
                shortUrl,
                ipAddress,
                userAgent,
                referrer,
                geoInfo.country,
                geoInfo.city,
                userAgentInfo.deviceType,
                userAgentInfo.browser,
                userAgentInfo.operatingSystem,
                userAgentInfo.isMobile,
                userAgentInfo.isBot
            );
            
            // Save analytics
            urlAnalyticsRepository.save(analytics);
            
            logger.debug("Successfully processed analytics for short URL: {}", shortUrl);
            
        } catch (Exception e) {
            logger.error("Failed to process analytics for URL: {}", shortUrl, e);
            // Don't fail the request if analytics processing fails
        }
    }
    
    @Override
    @Transactional(readOnly = true)
    public UrlAnalyticsResponse getUrlAnalytics(String shortUrl) {
        logger.debug("Getting analytics for short URL: {}", shortUrl);
        
        // Get URL mapping for basic info
        Optional<UrlMapping> urlMapping = urlMappingRepository.findByShortUrl(shortUrl);
        if (urlMapping.isEmpty()) {
            throw new RuntimeException("URL not found: " + shortUrl);
        }
        
        UrlMapping mapping = urlMapping.get();
        
        // Get analytics data
        Long totalClicks = urlAnalyticsRepository.countByShortUrl(shortUrl);
        Long uniqueClicks = urlAnalyticsRepository.countUniqueClicksByShortUrl(shortUrl);
        LocalDateTime firstClickAt = urlAnalyticsRepository.findFirstClickTime(shortUrl);
        LocalDateTime lastClickAt = urlAnalyticsRepository.findLastClickTime(shortUrl);
        
        // Get click distribution data
        Map<String, Long> clicksByCountry = convertToMap(urlAnalyticsRepository.getClicksByCountry(shortUrl));
        Map<String, Long> clicksByDevice = convertToMap(urlAnalyticsRepository.getClicksByDeviceType(shortUrl));
        Map<String, Long> clicksByBrowser = convertToMap(urlAnalyticsRepository.getClicksByBrowser(shortUrl));
        Map<String, Long> clicksByReferrer = convertToMap(urlAnalyticsRepository.getClicksByReferrer(shortUrl));
        Map<String, Long> clicksByDate = convertToMap(urlAnalyticsRepository.getClicksByDate(shortUrl));
        
        // Get recent clicks
        List<UrlAnalytics> recentClicksData = urlAnalyticsRepository.getRecentClicks(shortUrl, 10);
        List<UrlAnalyticsResponse.RecentClick> recentClicks = recentClicksData.stream()
            .map(this::mapToRecentClick)
            .collect(Collectors.toList());
        
        // Calculate click-through rate (simplified)
        Double clickThroughRate = totalClicks > 0 ? (double) uniqueClicks / totalClicks : 0.0;
        
        return UrlAnalyticsResponse.from(
            shortUrl,
            mapping.getOriginalUrl(),
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
            mapping.getCreatedAt()
        );
    }
    
    private String getClientIpAddress(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        
        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty()) {
            return xRealIp;
        }
        
        return request.getRemoteAddr();
    }
    
    private UserAgentInfo parseUserAgent(String userAgent) {
        if (userAgent == null || userAgent.trim().isEmpty()) {
            return new UserAgentInfo("Unknown", "Unknown", "Unknown", false, false);
        }
        
        String browser = parseBrowser(userAgent);
        String operatingSystem = parseOperatingSystem(userAgent);
        String deviceType = parseDeviceType(userAgent);
        boolean isMobile = userAgent.toLowerCase().contains("mobile") || 
                          userAgent.toLowerCase().contains("android") ||
                          userAgent.toLowerCase().contains("iphone");
        boolean isBot = userAgent.toLowerCase().contains("bot") ||
                       userAgent.toLowerCase().contains("crawler") ||
                       userAgent.toLowerCase().contains("spider");
        
        return new UserAgentInfo(browser, operatingSystem, deviceType, isMobile, isBot);
    }
    
    private String parseBrowser(String userAgent) {
        String ua = userAgent.toLowerCase();
        
        if (ua.contains("chrome") && !ua.contains("edge")) {
            return "Chrome";
        } else if (ua.contains("firefox")) {
            return "Firefox";
        } else if (ua.contains("safari") && !ua.contains("chrome")) {
            return "Safari";
        } else if (ua.contains("edge")) {
            return "Edge";
        } else if (ua.contains("opera")) {
            return "Opera";
        } else if (ua.contains("msie") || ua.contains("trident")) {
            return "Internet Explorer";
        }
        
        return "Unknown";
    }
    
    private String parseOperatingSystem(String userAgent) {
        String ua = userAgent.toLowerCase();
        
        if (ua.contains("windows")) {
            return "Windows";
        } else if (ua.contains("mac os x") || ua.contains("macintosh")) {
            return "macOS";
        } else if (ua.contains("linux")) {
            return "Linux";
        } else if (ua.contains("android")) {
            return "Android";
        } else if (ua.contains("ios") || ua.contains("iphone") || ua.contains("ipad")) {
            return "iOS";
        }
        
        return "Unknown";
    }
    
    private String parseDeviceType(String userAgent) {
        String ua = userAgent.toLowerCase();
        
        if (ua.contains("mobile") || ua.contains("android") || ua.contains("iphone")) {
            return "Mobile";
        } else if (ua.contains("tablet") || ua.contains("ipad")) {
            return "Tablet";
        }
        
        return "Desktop";
    }
    
    private GeoInfo getGeoInfo(String ipAddress) {
        // In a real system, you'd use a service like GeoIP2, MaxMind, or IP2Location
        // For now, return placeholder data
        return new GeoInfo("US", "Unknown");
    }
    
    private Map<String, Long> convertToMap(List<Object[]> data) {
        Map<String, Long> result = new HashMap<>();
        for (Object[] row : data) {
            String key = row[0] != null ? row[0].toString() : "Unknown";
            Long value = row[1] != null ? ((Number) row[1]).longValue() : 0L;
            result.put(key, value);
        }
        return result;
    }
    
    private UrlAnalyticsResponse.RecentClick mapToRecentClick(UrlAnalytics analytics) {
        return new UrlAnalyticsResponse.RecentClick(
            analytics.getIpAddress(),
            analytics.getUserAgent(),
            analytics.getReferrer(),
            analytics.getCountry(),
            analytics.getCity(),
            analytics.getDeviceType(),
            analytics.getBrowser(),
            analytics.getOperatingSystem(),
            analytics.getIsMobile(),
            analytics.getIsBot(),
            analytics.getClickedAt()
        );
    }
    
    // Helper classes for structured data
    private static class UserAgentInfo {
        String browser;
        String operatingSystem;
        String deviceType;
        boolean isMobile;
        boolean isBot;
        
        UserAgentInfo(String browser, String operatingSystem, String deviceType, boolean isMobile, boolean isBot) {
            this.browser = browser;
            this.operatingSystem = operatingSystem;
            this.deviceType = deviceType;
            this.isMobile = isMobile;
            this.isBot = isBot;
        }
    }
    
    private static class GeoInfo {
        String country;
        String city;
        
        GeoInfo(String country, String city) {
            this.country = country;
            this.city = city;
        }
    }
} 