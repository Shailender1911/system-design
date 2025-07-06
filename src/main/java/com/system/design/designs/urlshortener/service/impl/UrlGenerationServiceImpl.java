package com.system.design.designs.urlshortener.service.impl;

import com.system.design.designs.urlshortener.repository.UrlMappingRepository;
import com.system.design.designs.urlshortener.service.UrlGenerationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;

@Service
public class UrlGenerationServiceImpl implements UrlGenerationService {
    
    private static final Logger logger = LoggerFactory.getLogger(UrlGenerationServiceImpl.class);
    
    private static final String BASE62_CHARS = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
    private static final int DEFAULT_SHORT_URL_LENGTH = 7;
    private static final String COUNTER_KEY = "url_shortener_counter";
    private static final String RESERVED_ALIASES_KEY = "reserved_aliases";
    
    @Autowired
    private UrlMappingRepository urlMappingRepository;
    
    @Autowired
    private RedisTemplate<String, String> redisTemplate;
    
    @Value("${url.shortener.max-retries:5}")
    private int maxRetries;
    
    @Value("${url.shortener.salt:default-salt}")
    private String salt;
    
    // Reserved aliases that cannot be used
    private final Set<String> reservedAliases = new HashSet<>();
    
    public UrlGenerationServiceImpl() {
        initializeReservedAliases();
    }
    
    private void initializeReservedAliases() {
        reservedAliases.add("api");
        reservedAliases.add("admin");
        reservedAliases.add("help");
        reservedAliases.add("about");
        reservedAliases.add("contact");
        reservedAliases.add("support");
        reservedAliases.add("privacy");
        reservedAliases.add("terms");
        reservedAliases.add("login");
        reservedAliases.add("register");
        reservedAliases.add("dashboard");
        reservedAliases.add("profile");
        reservedAliases.add("settings");
        reservedAliases.add("analytics");
        reservedAliases.add("stats");
        reservedAliases.add("health");
        reservedAliases.add("status");
        reservedAliases.add("metrics");
        reservedAliases.add("docs");
        reservedAliases.add("www");
        reservedAliases.add("ftp");
        reservedAliases.add("mail");
        reservedAliases.add("webmail");
    }
    
    @Override
    public String generateShortUrl(String originalUrl) {
        return generateShortUrl(originalUrl, DEFAULT_SHORT_URL_LENGTH);
    }
    
    @Override
    public String generateShortUrl(String originalUrl, int length) {
        String shortUrl = null;
        int attempts = 0;
        
        while (attempts < maxRetries) {
            try {
                // Use MD5 hash with salt for better distribution
                String input = originalUrl + salt + System.currentTimeMillis() + attempts;
                MessageDigest md = MessageDigest.getInstance("MD5");
                byte[] hashBytes = md.digest(input.getBytes(StandardCharsets.UTF_8));
                
                // Convert to Base62
                String base62Hash = encodeToBase62(hashBytes, length);
                
                // Check if this short URL already exists
                if (!shortUrlExists(base62Hash)) {
                    shortUrl = base62Hash;
                    break;
                }
                
                attempts++;
                logger.debug("Collision detected for short URL: {}, retrying... (attempt {})", base62Hash, attempts);
                
            } catch (NoSuchAlgorithmException e) {
                logger.error("MD5 algorithm not available", e);
                throw new RuntimeException("Failed to generate short URL", e);
            }
        }
        
        if (shortUrl == null) {
            logger.error("Failed to generate unique short URL after {} attempts for URL: {}", maxRetries, originalUrl);
            throw new RuntimeException("Failed to generate unique short URL after " + maxRetries + " attempts");
        }
        
        logger.debug("Generated short URL: {} for original URL: {}", shortUrl, originalUrl);
        return shortUrl;
    }
    
    @Override
    public String generateShortUrlWithCounter() {
        try {
            // Get the next counter value from Redis
            Long counter = redisTemplate.opsForValue().increment(COUNTER_KEY);
            
            // Convert counter to Base62
            String shortUrl = encodeToBase62(counter);
            
            // Check for collision (unlikely but possible)
            if (shortUrlExists(shortUrl)) {
                // If collision, use recursive approach
                return generateShortUrlWithCounter();
            }
            
            logger.debug("Generated short URL with counter: {}", shortUrl);
            return shortUrl;
            
        } catch (Exception e) {
            logger.error("Failed to generate short URL with counter", e);
            throw new RuntimeException("Failed to generate short URL with counter", e);
        }
    }
    
    @Override
    public boolean isAliasAvailable(String alias) {
        if (alias == null || alias.trim().isEmpty()) {
            return false;
        }
        
        String normalizedAlias = alias.toLowerCase().trim();
        
        // Check if it's a reserved alias
        if (reservedAliases.contains(normalizedAlias)) {
            logger.debug("Alias {} is reserved", alias);
            return false;
        }
        
        // Check if it already exists in database
        boolean exists = urlMappingRepository.existsByCustomAlias(alias) || 
                        urlMappingRepository.existsByShortUrl(alias);
        
        logger.debug("Alias {} availability: {}", alias, !exists);
        return !exists;
    }
    
    @Override
    public boolean shortUrlExists(String shortUrl) {
        if (shortUrl == null || shortUrl.trim().isEmpty()) {
            return false;
        }
        
        // Check in cache first for performance
        String cacheKey = "short_url_exists:" + shortUrl;
        String cached = redisTemplate.opsForValue().get(cacheKey);
        
        if (cached != null) {
            return Boolean.parseBoolean(cached);
        }
        
        // Check in database
        boolean exists = urlMappingRepository.existsByShortUrl(shortUrl);
        
        // Cache the result for 5 minutes
        redisTemplate.opsForValue().set(cacheKey, String.valueOf(exists), 5, TimeUnit.MINUTES);
        
        return exists;
    }
    
    /**
     * Encode byte array to Base62 string
     * @param bytes the byte array
     * @param length the desired length
     * @return Base62 encoded string
     */
    private String encodeToBase62(byte[] bytes, int length) {
        // Convert bytes to a large number
        long number = 0;
        for (int i = 0; i < Math.min(bytes.length, 8); i++) {
            number = (number << 8) | (bytes[i] & 0xFF);
        }
        
        // Make sure it's positive
        number = Math.abs(number);
        
        return encodeToBase62(number, length);
    }
    
    /**
     * Encode long number to Base62 string
     * @param number the number
     * @return Base62 encoded string
     */
    private String encodeToBase62(long number) {
        return encodeToBase62(number, DEFAULT_SHORT_URL_LENGTH);
    }
    
    /**
     * Encode long number to Base62 string with specified length
     * @param number the number
     * @param length the desired length
     * @return Base62 encoded string
     */
    private String encodeToBase62(long number, int length) {
        if (number == 0) {
            return BASE62_CHARS.substring(0, 1);
        }
        
        StringBuilder sb = new StringBuilder();
        long num = Math.abs(number);
        
        while (num > 0) {
            sb.append(BASE62_CHARS.charAt((int)(num % 62)));
            num /= 62;
        }
        
        // Pad with zeros if needed
        while (sb.length() < length) {
            sb.append(BASE62_CHARS.charAt(0));
        }
        
        // Truncate if too long
        if (sb.length() > length) {
            sb.setLength(length);
        }
        
        return sb.reverse().toString();
    }
    
    /**
     * Decode Base62 string to long number
     * @param base62 the Base62 string
     * @return decoded number
     */
    public long decodeFromBase62(String base62) {
        long result = 0;
        long multiplier = 1;
        
        for (int i = base62.length() - 1; i >= 0; i--) {
            char c = base62.charAt(i);
            int index = BASE62_CHARS.indexOf(c);
            
            if (index == -1) {
                throw new IllegalArgumentException("Invalid Base62 character: " + c);
            }
            
            result += index * multiplier;
            multiplier *= 62;
        }
        
        return result;
    }
} 