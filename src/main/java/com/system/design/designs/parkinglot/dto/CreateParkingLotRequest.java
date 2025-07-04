package com.system.design.designs.parkinglot.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for creating parking lot request
 * 
 * @author Shailender Kumar
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateParkingLotRequest {
    
    @NotBlank(message = "Name is required")
    private String name;
    
    @NotBlank(message = "Location is required")
    private String location;
    
    @NotNull(message = "Total floors is required")
    @Min(value = 1, message = "Total floors must be at least 1")
    private Integer totalFloors;
    
    @NotNull(message = "Spots per floor is required")
    @Min(value = 1, message = "Spots per floor must be at least 1")
    private Integer spotsPerFloor;
} 