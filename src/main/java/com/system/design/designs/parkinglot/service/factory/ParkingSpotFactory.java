package com.system.design.designs.parkinglot.service.factory;

import com.system.design.designs.parkinglot.entity.ParkingFloor;
import com.system.design.designs.parkinglot.entity.ParkingSpot;
import com.system.design.designs.parkinglot.entity.SpotStatus;
import com.system.design.designs.parkinglot.entity.SpotType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Factory for creating parking spots
 * Implements Factory Pattern for object creation
 * 
 * @author Shailender Kumar
 */
@Component
@Slf4j
public class ParkingSpotFactory {
    
    /**
     * Create parking spots for a floor
     * 
     * @param floor parking floor
     * @param totalSpots total spots to create
     * @return list of created spots
     */
    public List<ParkingSpot> createSpotsForFloor(ParkingFloor floor, int totalSpots) {
        log.info("Creating {} spots for floor {}", totalSpots, floor.getFloorNumber());
        
        List<ParkingSpot> spots = new ArrayList<>();
        
        for (int spotNumber = 1; spotNumber <= totalSpots; spotNumber++) {
            ParkingSpot spot = createSpot(floor, spotNumber, totalSpots);
            spots.add(spot);
        }
        
        log.info("Created {} spots for floor {}", spots.size(), floor.getFloorNumber());
        return spots;
    }
    
    /**
     * Create a single parking spot
     * 
     * @param floor parking floor
     * @param spotNumber spot number
     * @param totalSpots total spots on floor
     * @return created parking spot
     */
    public ParkingSpot createSpot(ParkingFloor floor, int spotNumber, int totalSpots) {
        SpotType spotType = determineSpotType(spotNumber, totalSpots);
        String spotId = generateSpotId(floor.getFloorNumber(), spotNumber);
        
        return ParkingSpot.builder()
                .spotNumber(spotId)
                .spotType(spotType)
                .spotStatus(SpotStatus.AVAILABLE)
                .parkingFloor(floor)
                .build();
    }
    
    /**
     * Determine spot type based on distribution strategy
     * Distribution: 20% motorcycle, 60% compact, 15% large, 5% handicapped
     */
    private SpotType determineSpotType(int spotNumber, int totalSpots) {
        double ratio = (double) spotNumber / totalSpots;
        
        if (ratio <= 0.2) {
            return SpotType.MOTORCYCLE;
        } else if (ratio <= 0.8) {
            return SpotType.COMPACT;
        } else if (ratio <= 0.95) {
            return SpotType.LARGE;
        } else {
            return SpotType.HANDICAPPED;
        }
    }
    
    /**
     * Generate spot ID in format: F{floor}-S{spot}
     */
    private String generateSpotId(int floorNumber, int spotNumber) {
        return String.format("F%d-S%02d", floorNumber, spotNumber);
    }
    
    /**
     * Create spots with custom distribution
     */
    public List<ParkingSpot> createSpotsWithDistribution(
            ParkingFloor floor, 
            int motorcycleSpots, 
            int compactSpots, 
            int largeSpots, 
            int handicappedSpots) {
        
        log.info("Creating custom distribution spots for floor {}: M={}, C={}, L={}, H={}", 
                floor.getFloorNumber(), motorcycleSpots, compactSpots, largeSpots, handicappedSpots);
        
        List<ParkingSpot> spots = new ArrayList<>();
        int spotNumber = 1;
        
        // Create motorcycle spots
        spots.addAll(createSpotsByType(floor, SpotType.MOTORCYCLE, motorcycleSpots, spotNumber));
        spotNumber += motorcycleSpots;
        
        // Create compact spots
        spots.addAll(createSpotsByType(floor, SpotType.COMPACT, compactSpots, spotNumber));
        spotNumber += compactSpots;
        
        // Create large spots
        spots.addAll(createSpotsByType(floor, SpotType.LARGE, largeSpots, spotNumber));
        spotNumber += largeSpots;
        
        // Create handicapped spots
        spots.addAll(createSpotsByType(floor, SpotType.HANDICAPPED, handicappedSpots, spotNumber));
        
        log.info("Created {} spots with custom distribution for floor {}", 
                spots.size(), floor.getFloorNumber());
        
        return spots;
    }
    
    /**
     * Create spots of specific type
     */
    private List<ParkingSpot> createSpotsByType(ParkingFloor floor, SpotType spotType, int count, int startNumber) {
        List<ParkingSpot> spots = new ArrayList<>();
        
        for (int i = 0; i < count; i++) {
            int spotNumber = startNumber + i;
            String spotId = generateSpotId(floor.getFloorNumber(), spotNumber);
            
            ParkingSpot spot = ParkingSpot.builder()
                    .spotNumber(spotId)
                    .spotType(spotType)
                    .spotStatus(SpotStatus.AVAILABLE)
                    .parkingFloor(floor)
                    .build();
            
            spots.add(spot);
        }
        
        return spots;
    }
} 