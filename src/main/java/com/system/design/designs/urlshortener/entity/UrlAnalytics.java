package com.system.design.designs.urlshortener.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Table(name = "url_analytics", indexes = {
    @Index(name = "idx_short_url_analytics", columnList = "shortUrl"),
    @Index(name = "idx_clicked_at", columnList = "clickedAt"),
    @Index(name = "idx_user_agent", columnList = "userAgent"),
    @Index(name = "idx_country", columnList = "country"),
    @Index(name = "idx_referrer", columnList = "referrer")
})
public class UrlAnalytics {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @NotBlank
    @Column(name = "short_url", nullable = false, length = 10)
    private String shortUrl;
    
    @Column(name = "ip_address", length = 45) // IPv6 support
    private String ipAddress;
    
    @Column(name = "user_agent", columnDefinition = "TEXT")
    private String userAgent;
    
    @Column(name = "referrer", columnDefinition = "TEXT")
    private String referrer;
    
    @Column(name = "country", length = 2)
    private String country;
    
    @Column(name = "city", length = 100)
    private String city;
    
    @Column(name = "device_type", length = 50)
    private String deviceType;
    
    @Column(name = "browser", length = 50)
    private String browser;
    
    @Column(name = "os", length = 50)
    private String operatingSystem;
    
    @Column(name = "is_mobile", nullable = false, columnDefinition = "BOOLEAN DEFAULT FALSE")
    private Boolean isMobile = false;
    
    @Column(name = "is_bot", nullable = false, columnDefinition = "BOOLEAN DEFAULT FALSE")
    private Boolean isBot = false;
    
    @CreationTimestamp
    @Column(name = "clicked_at", nullable = false, updatable = false)
    private LocalDateTime clickedAt;
    
    // Constructors
    public UrlAnalytics() {}
    
    public UrlAnalytics(String shortUrl, String ipAddress, String userAgent, 
                       String referrer, String country, String city, 
                       String deviceType, String browser, String operatingSystem, 
                       Boolean isMobile, Boolean isBot) {
        this.shortUrl = shortUrl;
        this.ipAddress = ipAddress;
        this.userAgent = userAgent;
        this.referrer = referrer;
        this.country = country;
        this.city = city;
        this.deviceType = deviceType;
        this.browser = browser;
        this.operatingSystem = operatingSystem;
        this.isMobile = isMobile;
        this.isBot = isBot;
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getShortUrl() {
        return shortUrl;
    }
    
    public void setShortUrl(String shortUrl) {
        this.shortUrl = shortUrl;
    }
    
    public String getIpAddress() {
        return ipAddress;
    }
    
    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }
    
    public String getUserAgent() {
        return userAgent;
    }
    
    public void setUserAgent(String userAgent) {
        this.userAgent = userAgent;
    }
    
    public String getReferrer() {
        return referrer;
    }
    
    public void setReferrer(String referrer) {
        this.referrer = referrer;
    }
    
    public String getCountry() {
        return country;
    }
    
    public void setCountry(String country) {
        this.country = country;
    }
    
    public String getCity() {
        return city;
    }
    
    public void setCity(String city) {
        this.city = city;
    }
    
    public String getDeviceType() {
        return deviceType;
    }
    
    public void setDeviceType(String deviceType) {
        this.deviceType = deviceType;
    }
    
    public String getBrowser() {
        return browser;
    }
    
    public void setBrowser(String browser) {
        this.browser = browser;
    }
    
    public String getOperatingSystem() {
        return operatingSystem;
    }
    
    public void setOperatingSystem(String operatingSystem) {
        this.operatingSystem = operatingSystem;
    }
    
    public Boolean getIsMobile() {
        return isMobile;
    }
    
    public void setIsMobile(Boolean isMobile) {
        this.isMobile = isMobile;
    }
    
    public Boolean getIsBot() {
        return isBot;
    }
    
    public void setIsBot(Boolean isBot) {
        this.isBot = isBot;
    }
    
    public LocalDateTime getClickedAt() {
        return clickedAt;
    }
    
    public void setClickedAt(LocalDateTime clickedAt) {
        this.clickedAt = clickedAt;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UrlAnalytics that = (UrlAnalytics) o;
        return Objects.equals(id, that.id);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
    
    @Override
    public String toString() {
        return "UrlAnalytics{" +
                "id=" + id +
                ", shortUrl='" + shortUrl + '\'' +
                ", ipAddress='" + ipAddress + '\'' +
                ", userAgent='" + userAgent + '\'' +
                ", referrer='" + referrer + '\'' +
                ", country='" + country + '\'' +
                ", city='" + city + '\'' +
                ", deviceType='" + deviceType + '\'' +
                ", browser='" + browser + '\'' +
                ", operatingSystem='" + operatingSystem + '\'' +
                ", isMobile=" + isMobile +
                ", isBot=" + isBot +
                ", clickedAt=" + clickedAt +
                '}';
    }
} 