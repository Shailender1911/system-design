package com.system.design.designs.urlshortener.repository;

import com.system.design.designs.urlshortener.entity.UrlMapping;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface UrlMappingRepository extends JpaRepository<UrlMapping, Long> {
    
    /**
     * Find URL mapping by short URL
     * @param shortUrl the short URL
     * @return Optional UrlMapping
     */
    Optional<UrlMapping> findByShortUrl(String shortUrl);
    
    /**
     * Find URL mapping by short URL and active status
     * @param shortUrl the short URL
     * @param isActive the active status
     * @return Optional UrlMapping
     */
    Optional<UrlMapping> findByShortUrlAndIsActive(String shortUrl, Boolean isActive);
    
    /**
     * Find URL mapping by custom alias
     * @param customAlias the custom alias
     * @return Optional UrlMapping
     */
    Optional<UrlMapping> findByCustomAlias(String customAlias);
    
    /**
     * Find URL mappings by user ID
     * @param userId the user ID
     * @param pageable pagination parameters
     * @return Page of UrlMapping
     */
    Page<UrlMapping> findByUserId(String userId, Pageable pageable);
    
    /**
     * Find URL mappings by user ID and active status
     * @param userId the user ID
     * @param isActive the active status
     * @param pageable pagination parameters
     * @return Page of UrlMapping
     */
    Page<UrlMapping> findByUserIdAndIsActive(String userId, Boolean isActive, Pageable pageable);
    
    /**
     * Find expired URL mappings
     * @param currentTime the current time
     * @return List of expired UrlMapping
     */
    @Query("SELECT u FROM UrlMapping u WHERE u.expirationDate IS NOT NULL AND u.expirationDate < :currentTime AND u.isActive = true")
    List<UrlMapping> findExpiredUrls(@Param("currentTime") LocalDateTime currentTime);
    
    /**
     * Find URL mappings created within a time range
     * @param startDate start date
     * @param endDate end date
     * @return List of UrlMapping
     */
    @Query("SELECT u FROM UrlMapping u WHERE u.createdAt BETWEEN :startDate AND :endDate")
    List<UrlMapping> findByCreatedAtBetween(@Param("startDate") LocalDateTime startDate, 
                                          @Param("endDate") LocalDateTime endDate);
    
    /**
     * Find top URLs by click count
     * @param limit the limit
     * @return List of UrlMapping
     */
    @Query("SELECT u FROM UrlMapping u WHERE u.isActive = true ORDER BY u.clickCount DESC LIMIT :limit")
    List<UrlMapping> findTopUrlsByClickCount(@Param("limit") int limit);
    
    /**
     * Check if short URL exists
     * @param shortUrl the short URL
     * @return boolean
     */
    boolean existsByShortUrl(String shortUrl);
    
    /**
     * Check if custom alias exists
     * @param customAlias the custom alias
     * @return boolean
     */
    boolean existsByCustomAlias(String customAlias);
    
    /**
     * Increment click count for a URL
     * @param shortUrl the short URL
     * @return number of updated rows
     */
    @Modifying
    @Transactional
    @Query("UPDATE UrlMapping u SET u.clickCount = u.clickCount + 1, u.updatedAt = CURRENT_TIMESTAMP WHERE u.shortUrl = :shortUrl")
    int incrementClickCount(@Param("shortUrl") String shortUrl);
    
    /**
     * Deactivate expired URLs
     * @param currentTime the current time
     * @return number of updated rows
     */
    @Modifying
    @Transactional
    @Query("UPDATE UrlMapping u SET u.isActive = false, u.updatedAt = CURRENT_TIMESTAMP WHERE u.expirationDate IS NOT NULL AND u.expirationDate < :currentTime AND u.isActive = true")
    int deactivateExpiredUrls(@Param("currentTime") LocalDateTime currentTime);
    
    /**
     * Get total click count for a user
     * @param userId the user ID
     * @return total click count
     */
    @Query("SELECT COALESCE(SUM(u.clickCount), 0) FROM UrlMapping u WHERE u.userId = :userId AND u.isActive = true")
    Long getTotalClickCountByUserId(@Param("userId") String userId);
    
    /**
     * Get URL count by user ID
     * @param userId the user ID
     * @return URL count
     */
    @Query("SELECT COUNT(u) FROM UrlMapping u WHERE u.userId = :userId AND u.isActive = true")
    Long getUrlCountByUserId(@Param("userId") String userId);
    
    /**
     * Delete inactive URLs older than specified date
     * @param cutoffDate the cutoff date
     * @return number of deleted rows
     */
    @Modifying
    @Transactional
    @Query("DELETE FROM UrlMapping u WHERE u.isActive = false AND u.updatedAt < :cutoffDate")
    int deleteInactiveUrlsOlderThan(@Param("cutoffDate") LocalDateTime cutoffDate);
} 