package com.system.design.designs.parkinglot.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO for ParkingLot entity
 * 
 * @author Shailender Kumar
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ParkingLotDTO {
    
    private Long id;
    private String name;
    private String location;
    private Integer totalFloors;
    private Integer spotsPerFloor;
    private Integer totalSpots;
    private Integer availableSpots;
    private Boolean isActive;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedAt;
    
    /**
     * Calculate occupancy percentage
     */
    public Double getOccupancyPercentage() {
        if (totalSpots == 0) return 0.0;
        return ((double) (totalSpots - availableSpots) / totalSpots) * 100;
    }
} 