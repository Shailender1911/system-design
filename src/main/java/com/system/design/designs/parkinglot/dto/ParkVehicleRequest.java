package com.system.design.designs.parkinglot.dto;

import com.system.design.designs.parkinglot.entity.VehicleType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for parking vehicle request
 * 
 * @author Shailender Kumar
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ParkVehicleRequest {
    
    @NotBlank(message = "License plate is required")
    private String licensePlate;
    
    @NotNull(message = "Vehicle type is required")
    private VehicleType vehicleType;
    
    @NotNull(message = "Parking lot ID is required")
    private Long parkingLotId;
} 