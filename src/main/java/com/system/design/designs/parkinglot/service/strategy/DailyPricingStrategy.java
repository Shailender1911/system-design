package com.system.design.designs.parkinglot.service.strategy;

import com.system.design.designs.parkinglot.entity.VehicleType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.Duration;

/**
 * Daily pricing strategy implementation
 * Flat daily rate for long-term parking
 * 
 * @author Shailender Kumar
 */
@Component
@Slf4j
public class DailyPricingStrategy implements PricingStrategy {
    
    private static final BigDecimal DAILY_RATE = BigDecimal.valueOf(25.0);
    private static final BigDecimal HOURLY_RATE = BigDecimal.valueOf(5.0);
    private static final long HOURS_IN_DAY = 24;
    private static final BigDecimal WEEKEND_MULTIPLIER = BigDecimal.valueOf(1.1);
    
    // Vehicle type multipliers
    private static final BigDecimal MOTORCYCLE_MULTIPLIER = BigDecimal.valueOf(0.7);
    private static final BigDecimal CAR_MULTIPLIER = BigDecimal.valueOf(1.0);
    private static final BigDecimal TRUCK_MULTIPLIER = BigDecimal.valueOf(1.3);
    
    @Override
    public BigDecimal calculateFee(Duration duration, VehicleType vehicleType, boolean isWeekend) {
        log.info("Calculating daily fee for {} vehicle, duration: {} hours, weekend: {}", 
                vehicleType, duration.toHours(), isWeekend);
        
        long totalHours = duration.toHours();
        if (duration.toMinutes() % 60 > 0) {
            totalHours++;
        }
        
        BigDecimal totalFee;
        
        if (totalHours >= HOURS_IN_DAY) {
            // Calculate full days and remaining hours
            long days = totalHours / HOURS_IN_DAY;
            long remainingHours = totalHours % HOURS_IN_DAY;
            
            // Daily rate for full days + hourly rate for remaining hours
            BigDecimal dailyFee = DAILY_RATE.multiply(BigDecimal.valueOf(days));
            BigDecimal hourlyFee = HOURLY_RATE.multiply(BigDecimal.valueOf(remainingHours));
            
            totalFee = dailyFee.add(hourlyFee);
        } else {
            // Less than a day, use hourly rate
            totalFee = HOURLY_RATE.multiply(BigDecimal.valueOf(totalHours));
        }
        
        // Apply vehicle type multiplier
        BigDecimal vehicleMultiplier = getVehicleMultiplier(vehicleType);
        totalFee = totalFee.multiply(vehicleMultiplier);
        
        // Apply weekend multiplier
        if (isWeekend) {
            totalFee = totalFee.multiply(WEEKEND_MULTIPLIER);
        }
        
        log.info("Calculated daily fee: ${}", totalFee);
        return totalFee;
    }
    
    @Override
    public String getStrategyName() {
        return "DAILY";
    }
    
    @Override
    public boolean isApplicable(Duration duration, VehicleType vehicleType) {
        // Applicable for parking duration >= 6 hours
        return duration.toHours() >= 6;
    }
    
    private BigDecimal getVehicleMultiplier(VehicleType vehicleType) {
        return switch (vehicleType) {
            case MOTORCYCLE -> MOTORCYCLE_MULTIPLIER;
            case CAR -> CAR_MULTIPLIER;
            case TRUCK -> TRUCK_MULTIPLIER;
        };
    }
} 