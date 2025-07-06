package com.system.design.designs.urlshortener.repository;

import com.system.design.designs.urlshortener.entity.UrlAnalytics;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Repository
public interface UrlAnalyticsRepository extends JpaRepository<UrlAnalytics, Long> {
    
    /**
     * Find analytics by short URL
     * @param shortUrl the short URL
     * @param pageable pagination parameters
     * @return Page of UrlAnalytics
     */
    Page<UrlAnalytics> findByShortUrl(String shortUrl, Pageable pageable);
    
    /**
     * Find analytics by short URL within time range
     * @param shortUrl the short URL
     * @param startDate start date
     * @param endDate end date
     * @return List of UrlAnalytics
     */
    @Query("SELECT u FROM UrlAnalytics u WHERE u.shortUrl = :shortUrl AND u.clickedAt BETWEEN :startDate AND :endDate")
    List<UrlAnalytics> findByShortUrlAndClickedAtBetween(@Param("shortUrl") String shortUrl, 
                                                        @Param("startDate") LocalDateTime startDate, 
                                                        @Param("endDate") LocalDateTime endDate);
    
    /**
     * Count total clicks for a short URL
     * @param shortUrl the short URL
     * @return total click count
     */
    @Query("SELECT COUNT(u) FROM UrlAnalytics u WHERE u.shortUrl = :shortUrl")
    Long countByShortUrl(@Param("shortUrl") String shortUrl);
    
    /**
     * Count unique clicks for a short URL (by IP address)
     * @param shortUrl the short URL
     * @return unique click count
     */
    @Query("SELECT COUNT(DISTINCT u.ipAddress) FROM UrlAnalytics u WHERE u.shortUrl = :shortUrl")
    Long countUniqueClicksByShortUrl(@Param("shortUrl") String shortUrl);
    
    /**
     * Find first click time for a short URL
     * @param shortUrl the short URL
     * @return first click time
     */
    @Query("SELECT MIN(u.clickedAt) FROM UrlAnalytics u WHERE u.shortUrl = :shortUrl")
    LocalDateTime findFirstClickTime(@Param("shortUrl") String shortUrl);
    
    /**
     * Find last click time for a short URL
     * @param shortUrl the short URL
     * @return last click time
     */
    @Query("SELECT MAX(u.clickedAt) FROM UrlAnalytics u WHERE u.shortUrl = :shortUrl")
    LocalDateTime findLastClickTime(@Param("shortUrl") String shortUrl);
    
    /**
     * Get clicks by country for a short URL
     * @param shortUrl the short URL
     * @return Map of country to click count
     */
    @Query("SELECT u.country, COUNT(u) FROM UrlAnalytics u WHERE u.shortUrl = :shortUrl AND u.country IS NOT NULL GROUP BY u.country ORDER BY COUNT(u) DESC")
    List<Object[]> getClicksByCountry(@Param("shortUrl") String shortUrl);
    
    /**
     * Get clicks by device type for a short URL
     * @param shortUrl the short URL
     * @return Map of device type to click count
     */
    @Query("SELECT u.deviceType, COUNT(u) FROM UrlAnalytics u WHERE u.shortUrl = :shortUrl AND u.deviceType IS NOT NULL GROUP BY u.deviceType ORDER BY COUNT(u) DESC")
    List<Object[]> getClicksByDeviceType(@Param("shortUrl") String shortUrl);
    
    /**
     * Get clicks by browser for a short URL
     * @param shortUrl the short URL
     * @return Map of browser to click count
     */
    @Query("SELECT u.browser, COUNT(u) FROM UrlAnalytics u WHERE u.shortUrl = :shortUrl AND u.browser IS NOT NULL GROUP BY u.browser ORDER BY COUNT(u) DESC")
    List<Object[]> getClicksByBrowser(@Param("shortUrl") String shortUrl);
    
    /**
     * Get clicks by referrer for a short URL
     * @param shortUrl the short URL
     * @return Map of referrer to click count
     */
    @Query("SELECT u.referrer, COUNT(u) FROM UrlAnalytics u WHERE u.shortUrl = :shortUrl AND u.referrer IS NOT NULL GROUP BY u.referrer ORDER BY COUNT(u) DESC")
    List<Object[]> getClicksByReferrer(@Param("shortUrl") String shortUrl);
    
    /**
     * Get clicks by date for a short URL
     * @param shortUrl the short URL
     * @return Map of date to click count
     */
    @Query("SELECT DATE(u.clickedAt), COUNT(u) FROM UrlAnalytics u WHERE u.shortUrl = :shortUrl GROUP BY DATE(u.clickedAt) ORDER BY DATE(u.clickedAt) DESC")
    List<Object[]> getClicksByDate(@Param("shortUrl") String shortUrl);
    
    /**
     * Get recent clicks for a short URL
     * @param shortUrl the short URL
     * @param limit the limit
     * @return List of recent UrlAnalytics
     */
    @Query("SELECT u FROM UrlAnalytics u WHERE u.shortUrl = :shortUrl ORDER BY u.clickedAt DESC LIMIT :limit")
    List<UrlAnalytics> getRecentClicks(@Param("shortUrl") String shortUrl, @Param("limit") int limit);
    
    /**
     * Count bot clicks for a short URL
     * @param shortUrl the short URL
     * @return bot click count
     */
    @Query("SELECT COUNT(u) FROM UrlAnalytics u WHERE u.shortUrl = :shortUrl AND u.isBot = true")
    Long countBotClicksByShortUrl(@Param("shortUrl") String shortUrl);
    
    /**
     * Count mobile clicks for a short URL
     * @param shortUrl the short URL
     * @return mobile click count
     */
    @Query("SELECT COUNT(u) FROM UrlAnalytics u WHERE u.shortUrl = :shortUrl AND u.isMobile = true")
    Long countMobileClicksByShortUrl(@Param("shortUrl") String shortUrl);
    
    /**
     * Get top referring domains for a short URL
     * @param shortUrl the short URL
     * @param limit the limit
     * @return List of top referring domains
     */
    @Query("SELECT SUBSTRING_INDEX(SUBSTRING_INDEX(u.referrer, '/', 3), '/', -1) as domain, COUNT(u) as clickCount " +
           "FROM UrlAnalytics u WHERE u.shortUrl = :shortUrl AND u.referrer IS NOT NULL AND u.referrer != '' " +
           "GROUP BY domain ORDER BY clickCount DESC LIMIT :limit")
    List<Object[]> getTopReferringDomains(@Param("shortUrl") String shortUrl, @Param("limit") int limit);
    
    /**
     * Get hourly click distribution for a short URL
     * @param shortUrl the short URL
     * @return Map of hour to click count
     */
    @Query("SELECT HOUR(u.clickedAt), COUNT(u) FROM UrlAnalytics u WHERE u.shortUrl = :shortUrl GROUP BY HOUR(u.clickedAt) ORDER BY HOUR(u.clickedAt)")
    List<Object[]> getHourlyClickDistribution(@Param("shortUrl") String shortUrl);
    
    /**
     * Delete analytics older than specified date
     * @param cutoffDate the cutoff date
     * @return number of deleted rows
     */
    @Query("DELETE FROM UrlAnalytics u WHERE u.clickedAt < :cutoffDate")
    int deleteAnalyticsOlderThan(@Param("cutoffDate") LocalDateTime cutoffDate);
} 