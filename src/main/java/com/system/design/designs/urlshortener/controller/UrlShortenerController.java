package com.system.design.designs.urlshortener.controller;

import com.system.design.common.dto.ApiResponse;
import com.system.design.designs.urlshortener.dto.ShortenUrlRequest;
import com.system.design.designs.urlshortener.dto.ShortenUrlResponse;
import com.system.design.designs.urlshortener.dto.UrlAnalyticsResponse;
import com.system.design.designs.urlshortener.service.UrlAnalyticsService;
import com.system.design.designs.urlshortener.service.UrlShortenerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/url")
@Tag(name = "URL Shortener", description = "URL shortening and management operations")
@Validated
public class UrlShortenerController {
    
    private static final Logger logger = LoggerFactory.getLogger(UrlShortenerController.class);
    
    @Autowired
    private UrlShortenerService urlShortenerService;
    
    @Autowired
    private UrlAnalyticsService urlAnalyticsService;
    
    @Operation(summary = "Shorten a URL", description = "Create a shortened URL for a given long URL")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "URL shortened successfully"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid request"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "Custom alias already exists")
    })
    @PostMapping("/shorten")
    public ResponseEntity<ApiResponse<ShortenUrlResponse>> shortenUrl(
            @Valid @RequestBody ShortenUrlRequest request,
            @RequestHeader(value = "X-User-ID", required = false) String userId) {
        
        logger.info("Received request to shorten URL: {}", request.originalUrl());
        
        try {
            // Add user ID to request if provided
            ShortenUrlRequest requestWithUser = userId != null ? 
                new ShortenUrlRequest(request.originalUrl(), request.customAlias(), userId, 
                                    request.expirationDate(), request.description()) : 
                request;
            
            ShortenUrlResponse response = urlShortenerService.shortenUrl(requestWithUser);
            
            logger.info("Successfully shortened URL: {} -> {}", request.originalUrl(), response.shortUrl());
            
            return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("URL shortened successfully", response));
                
        } catch (Exception e) {
            logger.error("Failed to shorten URL: {}", request.originalUrl(), e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(e.getMessage()));
        }
    }
    
    @Operation(summary = "Get URL details", description = "Get details of a shortened URL")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "URL details retrieved successfully"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "URL not found")
    })
    @GetMapping("/{shortUrl}")
    public ResponseEntity<ApiResponse<ShortenUrlResponse>> getUrlDetails(
            @Parameter(description = "Short URL identifier") @PathVariable String shortUrl) {
        
        logger.debug("Getting URL details for: {}", shortUrl);
        
        Optional<ShortenUrlResponse> response = urlShortenerService.getUrlDetails(shortUrl);
        
        if (response.isPresent()) {
            return ResponseEntity.ok(ApiResponse.success("URL details retrieved successfully", response.get()));
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.error("URL not found"));
        }
    }
    
    @Operation(summary = "Get user URLs", description = "Get all URLs created by a user")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "User URLs retrieved successfully"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid request")
    })
    @GetMapping("/user/{userId}")
    public ResponseEntity<ApiResponse<Page<ShortenUrlResponse>>> getUserUrls(
            @Parameter(description = "User ID") @PathVariable String userId,
            @PageableDefault(size = 20) Pageable pageable) {
        
        logger.debug("Getting URLs for user: {}", userId);
        
        try {
            Page<ShortenUrlResponse> response = urlShortenerService.getUserUrls(userId, pageable);
            
            return ResponseEntity.ok(ApiResponse.success("User URLs retrieved successfully", response));
            
        } catch (Exception e) {
            logger.error("Failed to get URLs for user: {}", userId, e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(e.getMessage()));
        }
    }
    
    @Operation(summary = "Get URL analytics", description = "Get analytics data for a shortened URL")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Analytics retrieved successfully"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "URL not found")
    })
    @GetMapping("/{shortUrl}/analytics")
    public ResponseEntity<ApiResponse<UrlAnalyticsResponse>> getUrlAnalytics(
            @Parameter(description = "Short URL identifier") @PathVariable String shortUrl) {
        
        logger.debug("Getting analytics for URL: {}", shortUrl);
        
        try {
            UrlAnalyticsResponse response = urlShortenerService.getUrlAnalytics(shortUrl);
            
            return ResponseEntity.ok(ApiResponse.success("Analytics retrieved successfully", response));
            
        } catch (Exception e) {
            logger.error("Failed to get analytics for URL: {}", shortUrl, e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.error(e.getMessage()));
        }
    }
    
    @Operation(summary = "Update URL status", description = "Activate or deactivate a shortened URL")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "URL status updated successfully"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "URL not found")
    })
    @PutMapping("/{shortUrl}/status")
    public ResponseEntity<ApiResponse<ShortenUrlResponse>> updateUrlStatus(
            @Parameter(description = "Short URL identifier") @PathVariable String shortUrl,
            @Parameter(description = "Active status") @RequestParam boolean active) {
        
        logger.info("Updating URL status for {}: active={}", shortUrl, active);
        
        try {
            ShortenUrlResponse response = urlShortenerService.updateUrlStatus(shortUrl, active);
            
            return ResponseEntity.ok(ApiResponse.success(response, "URL status updated successfully"));
            
        } catch (Exception e) {
            logger.error("Failed to update URL status for: {}", shortUrl, e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.error(e.getMessage()));
        }
    }
    
    @Operation(summary = "Delete URL", description = "Delete/deactivate a shortened URL")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "URL deleted successfully"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "URL not found"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Unauthorized")
    })
    @DeleteMapping("/{shortUrl}")
    public ResponseEntity<ApiResponse<Void>> deleteUrl(
            @Parameter(description = "Short URL identifier") @PathVariable String shortUrl,
            @RequestHeader(value = "X-User-ID", required = false) String userId) {
        
        logger.info("Deleting URL: {} for user: {}", shortUrl, userId);
        
        try {
            boolean deleted = urlShortenerService.deleteUrl(shortUrl, userId);
            
            if (deleted) {
                return ResponseEntity.ok(ApiResponse.success(null, "URL deleted successfully"));
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error("URL not found"));
            }
            
        } catch (Exception e) {
            logger.error("Failed to delete URL: {}", shortUrl, e);
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(ApiResponse.error(e.getMessage()));
        }
    }
    
    @Operation(summary = "Check alias availability", description = "Check if a custom alias is available")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Alias availability checked"),
    })
    @GetMapping("/check-alias/{alias}")
    public ResponseEntity<ApiResponse<Boolean>> checkAliasAvailability(
            @Parameter(description = "Custom alias to check") @PathVariable String alias) {
        
        logger.debug("Checking alias availability for: {}", alias);
        
        try {
            boolean available = urlShortenerService.isAliasAvailable(alias);
            
            return ResponseEntity.ok(ApiResponse.success(available, 
                available ? "Alias is available" : "Alias is not available"));
            
        } catch (Exception e) {
            logger.error("Failed to check alias availability for: {}", alias, e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(e.getMessage()));
        }
    }
    
    @Operation(summary = "Cleanup expired URLs", description = "Clean up expired URLs (Admin only)")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Cleanup completed"),
    })
    @PostMapping("/admin/cleanup")
    public ResponseEntity<ApiResponse<Integer>> cleanupExpiredUrls() {
        
        logger.info("Starting cleanup of expired URLs");
        
        try {
            int cleanedCount = urlShortenerService.cleanupExpiredUrls();
            
            return ResponseEntity.ok(ApiResponse.success(cleanedCount, 
                "Cleaned up " + cleanedCount + " expired URLs"));
            
        } catch (Exception e) {
            logger.error("Failed to cleanup expired URLs", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error(e.getMessage()));
        }
    }
}

@RestController
@RequestMapping("/")
@Tag(name = "URL Redirection", description = "URL redirection operations")
class UrlRedirectionController {
    
    private static final Logger logger = LoggerFactory.getLogger(UrlRedirectionController.class);
    
    @Autowired
    private UrlShortenerService urlShortenerService;
    
    @Autowired
    private UrlAnalyticsService urlAnalyticsService;
    
    @Operation(summary = "Redirect to original URL", description = "Redirect to the original URL using short URL")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "301", description = "Redirected to original URL"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "URL not found"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "410", description = "URL expired")
    })
    @GetMapping("/{shortUrl}")
    public void redirectToOriginalUrl(
            @Parameter(description = "Short URL identifier") @PathVariable String shortUrl,
            HttpServletRequest request,
            HttpServletResponse response) throws IOException {
        
        logger.debug("Redirecting short URL: {}", shortUrl);
        
        try {
            Optional<String> originalUrl = urlShortenerService.getOriginalUrl(shortUrl);
            
            if (originalUrl.isPresent()) {
                // Record analytics asynchronously
                urlAnalyticsService.recordClick(shortUrl, request);
                
                // Redirect to original URL
                response.setStatus(HttpStatus.MOVED_PERMANENTLY.value());
                response.setHeader("Location", originalUrl.get());
                response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
                
                logger.debug("Successfully redirected {} to {}", shortUrl, originalUrl.get());
                
            } else {
                logger.warn("Short URL not found or expired: {}", shortUrl);
                response.setStatus(HttpStatus.NOT_FOUND.value());
                response.getWriter().write("URL not found or expired");
            }
            
        } catch (Exception e) {
            logger.error("Failed to redirect URL: {}", shortUrl, e);
            response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
            response.getWriter().write("Internal server error");
        }
    }
} 