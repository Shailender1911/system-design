package com.system.design.designs.parkinglot.service.strategy;

import com.system.design.designs.parkinglot.entity.VehicleType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Context class for managing pricing strategies
 * Implements Strategy Pattern for flexible pricing
 * 
 * @author Shailender Kumar
 */
@Component
@Slf4j
public class PricingContext {
    
    private final List<PricingStrategy> strategies;
    
    public PricingContext(List<PricingStrategy> strategies) {
        this.strategies = strategies;
        log.info("Initialized pricing context with {} strategies", strategies.size());
    }
    
    /**
     * Calculate fee using the best applicable strategy
     * 
     * @param entryTime when vehicle entered
     * @param exitTime when vehicle exited
     * @param vehicleType type of vehicle
     * @return calculated fee
     */
    public BigDecimal calculateFee(LocalDateTime entryTime, LocalDateTime exitTime, VehicleType vehicleType) {
        Duration duration = Duration.between(entryTime, exitTime);
        boolean isWeekend = isWeekend(entryTime, exitTime);
        
        log.info("Calculating fee for {} vehicle, duration: {} minutes, weekend: {}", 
                vehicleType, duration.toMinutes(), isWeekend);
        
        // Find the best applicable strategy
        PricingStrategy bestStrategy = findBestStrategy(duration, vehicleType);
        
        if (bestStrategy == null) {
            log.warn("No applicable pricing strategy found, using default hourly");
            return getDefaultStrategy().calculateFee(duration, vehicleType, isWeekend);
        }
        
        log.info("Using pricing strategy: {}", bestStrategy.getStrategyName());
        return bestStrategy.calculateFee(duration, vehicleType, isWeekend);
    }
    
    /**
     * Find the best pricing strategy for given conditions
     * Priority: Daily > Hourly (for cost optimization)
     */
    private PricingStrategy findBestStrategy(Duration duration, VehicleType vehicleType) {
        // First, try daily pricing if applicable (usually better for customer)
        for (PricingStrategy strategy : strategies) {
            if ("DAILY".equals(strategy.getStrategyName()) && 
                strategy.isApplicable(duration, vehicleType)) {
                return strategy;
            }
        }
        
        // Fall back to hourly pricing
        for (PricingStrategy strategy : strategies) {
            if ("HOURLY".equals(strategy.getStrategyName()) && 
                strategy.isApplicable(duration, vehicleType)) {
                return strategy;
            }
        }
        
        return null;
    }
    
    /**
     * Get default strategy (hourly)
     */
    private PricingStrategy getDefaultStrategy() {
        return strategies.stream()
                .filter(s -> "HOURLY".equals(s.getStrategyName()))
                .findFirst()
                .orElse(strategies.get(0));
    }
    
    /**
     * Check if parking period includes weekend
     */
    private boolean isWeekend(LocalDateTime entryTime, LocalDateTime exitTime) {
        LocalDateTime current = entryTime;
        while (current.isBefore(exitTime)) {
            int dayOfWeek = current.getDayOfWeek().getValue();
            if (dayOfWeek == 6 || dayOfWeek == 7) { // Saturday or Sunday
                return true;
            }
            current = current.plusDays(1);
        }
        return false;
    }
    
    /**
     * Get all available strategies
     */
    public List<PricingStrategy> getAllStrategies() {
        return strategies;
    }
    
    /**
     * Get strategy by name
     */
    public PricingStrategy getStrategy(String strategyName) {
        return strategies.stream()
                .filter(s -> s.getStrategyName().equals(strategyName))
                .findFirst()
                .orElse(null);
    }
} 