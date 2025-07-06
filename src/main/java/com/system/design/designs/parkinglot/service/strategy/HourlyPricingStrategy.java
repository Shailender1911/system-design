package com.system.design.designs.parkinglot.service.strategy;

import com.system.design.designs.parkinglot.entity.VehicleType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.Duration;

/**
 * Hourly pricing strategy implementation
 * Standard hourly rates with minimum charge
 * 
 * @author Shailender Kumar
 */
@Component
@Slf4j
public class HourlyPricingStrategy implements PricingStrategy {
    
    private static final BigDecimal HOURLY_RATE = BigDecimal.valueOf(5.0);
    private static final BigDecimal MINIMUM_CHARGE = BigDecimal.valueOf(2.0);
    private static final BigDecimal WEEKEND_MULTIPLIER = BigDecimal.valueOf(1.2);
    
    // Vehicle type multipliers
    private static final BigDecimal MOTORCYCLE_MULTIPLIER = BigDecimal.valueOf(0.8);
    private static final BigDecimal CAR_MULTIPLIER = BigDecimal.valueOf(1.0);
    private static final BigDecimal TRUCK_MULTIPLIER = BigDecimal.valueOf(1.5);
    
    @Override
    public BigDecimal calculateFee(Duration duration, VehicleType vehicleType, boolean isWeekend) {
        log.info("Calculating hourly fee for {} vehicle, duration: {} minutes, weekend: {}", 
                vehicleType, duration.toMinutes(), isWeekend);
        
        // Round up to next hour
        long hours = duration.toHours();
        if (duration.toMinutes() % 60 > 0) {
            hours++;
        }
        
        // Calculate base fee
        BigDecimal baseFee = HOURLY_RATE.multiply(BigDecimal.valueOf(hours));
        
        // Apply vehicle type multiplier
        BigDecimal vehicleMultiplier = getVehicleMultiplier(vehicleType);
        baseFee = baseFee.multiply(vehicleMultiplier);
        
        // Apply weekend multiplier
        if (isWeekend) {
            baseFee = baseFee.multiply(WEEKEND_MULTIPLIER);
        }
        
        // Ensure minimum charge
        BigDecimal finalFee = baseFee.max(MINIMUM_CHARGE);
        
        log.info("Calculated fee: ${}", finalFee);
        return finalFee;
    }
    
    @Override
    public String getStrategyName() {
        return "HOURLY";
    }
    
    @Override
    public boolean isApplicable(Duration duration, VehicleType vehicleType) {
        // Always applicable for hourly pricing
        return true;
    }
    
    private BigDecimal getVehicleMultiplier(VehicleType vehicleType) {
        return switch (vehicleType) {
            case MOTORCYCLE -> MOTORCYCLE_MULTIPLIER;
            case CAR -> CAR_MULTIPLIER;
            case TRUCK -> TRUCK_MULTIPLIER;
        };
    }
} 