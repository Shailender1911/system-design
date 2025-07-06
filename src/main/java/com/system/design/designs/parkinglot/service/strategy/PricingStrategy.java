package com.system.design.designs.parkinglot.service.strategy;

import com.system.design.designs.parkinglot.entity.VehicleType;

import java.math.BigDecimal;
import java.time.Duration;

/**
 * Strategy interface for different pricing strategies
 * Implements Strategy Pattern for Open/Closed Principle
 * 
 * @author Shailender Kumar
 */
public interface PricingStrategy {
    
    /**
     * Calculate parking fee based on strategy
     * @param duration parking duration
     * @param vehicleType type of vehicle
     * @param isWeekend whether it's weekend
     * @return calculated fee
     */
    BigDecimal calculateFee(Duration duration, VehicleType vehicleType, boolean isWeekend);
    
    /**
     * Get strategy name
     * @return strategy name
     */
    String getStrategyName();
    
    /**
     * Check if strategy is applicable for given conditions
     * @param duration parking duration
     * @param vehicleType type of vehicle
     * @return true if applicable
     */
    boolean isApplicable(Duration duration, VehicleType vehicleType);
} 