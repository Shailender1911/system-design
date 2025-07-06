package com.system.design.designs.urlshortener.service.impl;

import com.system.design.common.exception.BusinessException;
import com.system.design.common.exception.ResourceNotFoundException;
import com.system.design.designs.urlshortener.dto.ShortenUrlRequest;
import com.system.design.designs.urlshortener.dto.ShortenUrlResponse;
import com.system.design.designs.urlshortener.dto.UrlAnalyticsResponse;
import com.system.design.designs.urlshortener.entity.UrlMapping;
import com.system.design.designs.urlshortener.repository.UrlMappingRepository;
import com.system.design.designs.urlshortener.service.UrlAnalyticsService;
import com.system.design.designs.urlshortener.service.UrlGenerationService;
import com.system.design.designs.urlshortener.service.UrlShortenerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

@Service
@Transactional
public class UrlShortenerServiceImpl implements UrlShortenerService {
    
    private static final Logger logger = LoggerFactory.getLogger(UrlShortenerServiceImpl.class);
    
    @Autowired
    private UrlMappingRepository urlMappingRepository;
    
    @Autowired
    private UrlGenerationService urlGenerationService;
    
    @Autowired
    private UrlAnalyticsService urlAnalyticsService;
    
    @Autowired
    private RedisTemplate<String, String> redisTemplate;
    
    @Value("${url.shortener.base-url:https://short.ly/}")
    private String baseUrl;
    
    @Value("${url.shortener.cache-ttl:3600}")
    private int cacheTimeToLive;
    
    @Value("${url.shortener.max-urls-per-user:10000}")
    private int maxUrlsPerUser;
    
    @Override
    public ShortenUrlResponse shortenUrl(ShortenUrlRequest request) {
        logger.info("Creating shortened URL for: {}", request.originalUrl());
        
        // Validate request
        validateShortenRequest(request);
        
        // Check if user has reached the limit
        if (request.userId() != null) {
            Long userUrlCount = urlMappingRepository.getUrlCountByUserId(request.userId());
            if (userUrlCount >= maxUrlsPerUser) {
                throw new BusinessException("User has reached maximum URL limit: " + maxUrlsPerUser);
            }
        }
        
        // Generate short URL
        String shortUrl = generateShortUrl(request);
        
        // Create URL mapping
        UrlMapping urlMapping = new UrlMapping(
            shortUrl,
            request.originalUrl(),
            request.userId(),
            request.customAlias(),
            request.expirationDate(),
            request.description()
        );
        
        // Save to database
        urlMapping = urlMappingRepository.save(urlMapping);
        
        // Cache the mapping for quick access
        cacheUrlMapping(urlMapping);
        
        logger.info("Successfully created shortened URL: {} for original URL: {}", shortUrl, request.originalUrl());
        
        return mapToResponse(urlMapping);
    }
    
    @Override
    @Transactional(readOnly = true)
    public Optional<String> getOriginalUrl(String shortUrl) {
        logger.debug("Getting original URL for short URL: {}", shortUrl);
        
        // Check cache first
        String cacheKey = "url_mapping:" + shortUrl;
        String cachedUrl = redisTemplate.opsForValue().get(cacheKey);
        
        if (cachedUrl != null) {
            logger.debug("Found URL in cache: {}", cachedUrl);
            return Optional.of(cachedUrl);
        }
        
        // Query database
        Optional<UrlMapping> urlMapping = urlMappingRepository.findByShortUrlAndIsActive(shortUrl, true);
        
        if (urlMapping.isPresent()) {
            UrlMapping mapping = urlMapping.get();
            
            // Check if expired
            if (mapping.isExpired()) {
                logger.info("URL {} is expired", shortUrl);
                // Deactivate expired URL
                mapping.deactivate();
                urlMappingRepository.save(mapping);
                return Optional.empty();
            }
            
            // Cache the result
            redisTemplate.opsForValue().set(cacheKey, mapping.getOriginalUrl(), cacheTimeToLive, TimeUnit.SECONDS);
            
            // Increment click count asynchronously
            incrementClickCountAsync(shortUrl);
            
            logger.debug("Found original URL: {} for short URL: {}", mapping.getOriginalUrl(), shortUrl);
            return Optional.of(mapping.getOriginalUrl());
        }
        
        logger.debug("No URL found for short URL: {}", shortUrl);
        return Optional.empty();
    }
    
    @Override
    @Transactional(readOnly = true)
    public Optional<ShortenUrlResponse> getUrlDetails(String shortUrl) {
        logger.debug("Getting URL details for short URL: {}", shortUrl);
        
        Optional<UrlMapping> urlMapping = urlMappingRepository.findByShortUrl(shortUrl);
        
        if (urlMapping.isPresent()) {
            return Optional.of(mapToResponse(urlMapping.get()));
        }
        
        return Optional.empty();
    }
    
    @Override
    @Transactional(readOnly = true)
    public Page<ShortenUrlResponse> getUserUrls(String userId, Pageable pageable) {
        logger.debug("Getting URLs for user: {}", userId);
        
        Page<UrlMapping> urlMappings = urlMappingRepository.findByUserIdAndIsActive(userId, true, pageable);
        
        return urlMappings.map(this::mapToResponse);
    }
    
    @Override
    @Transactional(readOnly = true)
    public UrlAnalyticsResponse getUrlAnalytics(String shortUrl) {
        logger.debug("Getting analytics for short URL: {}", shortUrl);
        
        // Verify URL exists
        Optional<UrlMapping> urlMapping = urlMappingRepository.findByShortUrl(shortUrl);
        if (urlMapping.isEmpty()) {
            throw new ResourceNotFoundException("Short URL not found: " + shortUrl);
        }
        
        return urlAnalyticsService.getUrlAnalytics(shortUrl);
    }
    
    @Override
    public ShortenUrlResponse updateUrlStatus(String shortUrl, boolean isActive) {
        logger.info("Updating URL status for {}: active={}", shortUrl, isActive);
        
        Optional<UrlMapping> urlMapping = urlMappingRepository.findByShortUrl(shortUrl);
        
        if (urlMapping.isEmpty()) {
            throw new ResourceNotFoundException("Short URL not found: " + shortUrl);
        }
        
        UrlMapping mapping = urlMapping.get();
        mapping.setIsActive(isActive);
        mapping = urlMappingRepository.save(mapping);
        
        // Update cache
        if (isActive) {
            cacheUrlMapping(mapping);
        } else {
            // Remove from cache
            String cacheKey = "url_mapping:" + shortUrl;
            redisTemplate.delete(cacheKey);
        }
        
        logger.info("Successfully updated URL status for {}: active={}", shortUrl, isActive);
        
        return mapToResponse(mapping);
    }
    
    @Override
    public boolean deleteUrl(String shortUrl, String userId) {
        logger.info("Deleting URL: {} for user: {}", shortUrl, userId);
        
        Optional<UrlMapping> urlMapping = urlMappingRepository.findByShortUrl(shortUrl);
        
        if (urlMapping.isEmpty()) {
            throw new ResourceNotFoundException("Short URL not found: " + shortUrl);
        }
        
        UrlMapping mapping = urlMapping.get();
        
        // Check if user owns this URL
        if (userId != null && !userId.equals(mapping.getUserId())) {
            throw new BusinessException("User does not have permission to delete this URL");
        }
        
        // Soft delete by deactivating
        mapping.deactivate();
        urlMappingRepository.save(mapping);
        
        // Remove from cache
        String cacheKey = "url_mapping:" + shortUrl;
        redisTemplate.delete(cacheKey);
        
        logger.info("Successfully deleted URL: {}", shortUrl);
        
        return true;
    }
    
    @Override
    public boolean isAliasAvailable(String alias) {
        return urlGenerationService.isAliasAvailable(alias);
    }
    
    @Override
    public int cleanupExpiredUrls() {
        logger.info("Starting cleanup of expired URLs");
        
        LocalDateTime currentTime = LocalDateTime.now();
        int deactivatedCount = urlMappingRepository.deactivateExpiredUrls(currentTime);
        
        logger.info("Deactivated {} expired URLs", deactivatedCount);
        
        return deactivatedCount;
    }
    
    private void validateShortenRequest(ShortenUrlRequest request) {
        if (request.originalUrl() == null || request.originalUrl().trim().isEmpty()) {
            throw new BusinessException("Original URL cannot be empty");
        }
        
        // Validate URL format
        if (!request.originalUrl().startsWith("http://") && !request.originalUrl().startsWith("https://")) {
            throw new BusinessException("URL must start with http:// or https://");
        }
        
        // Validate custom alias if provided
        if (request.customAlias() != null && !request.customAlias().trim().isEmpty()) {
            if (!urlGenerationService.isAliasAvailable(request.customAlias())) {
                throw new BusinessException("Custom alias is not available: " + request.customAlias());
            }
        }
        
        // Validate expiration date
        if (request.expirationDate() != null && request.expirationDate().isBefore(LocalDateTime.now())) {
            throw new BusinessException("Expiration date must be in the future");
        }
    }
    
    private String generateShortUrl(ShortenUrlRequest request) {
        if (request.customAlias() != null && !request.customAlias().trim().isEmpty()) {
            return request.customAlias().trim();
        }
        
        // Use counter-based generation for better performance
        return urlGenerationService.generateShortUrlWithCounter();
    }
    
    private void cacheUrlMapping(UrlMapping urlMapping) {
        String cacheKey = "url_mapping:" + urlMapping.getShortUrl();
        redisTemplate.opsForValue().set(cacheKey, urlMapping.getOriginalUrl(), cacheTimeToLive, TimeUnit.SECONDS);
        
        // Cache URL details for quick access
        String detailsCacheKey = "url_details:" + urlMapping.getShortUrl();
        redisTemplate.opsForValue().set(detailsCacheKey, urlMapping.toString(), cacheTimeToLive, TimeUnit.SECONDS);
    }
    
    private void incrementClickCountAsync(String shortUrl) {
        // This should be done asynchronously to avoid blocking the redirect
        // In a real system, you'd use @Async or a message queue
        try {
            urlMappingRepository.incrementClickCount(shortUrl);
        } catch (Exception e) {
            logger.error("Failed to increment click count for URL: {}", shortUrl, e);
            // Don't fail the request if analytics update fails
        }
    }
    
    private ShortenUrlResponse mapToResponse(UrlMapping urlMapping) {
        return ShortenUrlResponse.from(
            urlMapping.getShortUrl(),
            urlMapping.getOriginalUrl(),
            urlMapping.getCustomAlias(),
            urlMapping.getUserId(),
            urlMapping.getExpirationDate(),
            urlMapping.getDescription(),
            urlMapping.getClickCount(),
            urlMapping.getIsActive(),
            urlMapping.getCreatedAt()
        );
    }
} 