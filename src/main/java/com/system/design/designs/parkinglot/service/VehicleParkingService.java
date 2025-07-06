package com.system.design.designs.parkinglot.service;

import com.system.design.designs.parkinglot.dto.ParkVehicleRequest;
import com.system.design.designs.parkinglot.dto.ParkingTicketDTO;

import java.util.List;

/**
 * Service interface for vehicle parking operations
 * Follows Single Responsibility Principle - only manages vehicle parking/exit
 * 
 * @author Shailender Kumar
 */
public interface VehicleParkingService {
    
    /**
     * Park a vehicle in available spot
     */
    ParkingTicketDTO parkVehicle(ParkVehicleRequest request);
    
    /**
     * Exit vehicle from parking spot
     */
    ParkingTicketDTO exitVehicle(String ticketNumber);
    
    /**
     * Get ticket by ticket number
     */
    ParkingTicketDTO getTicketByTicketNumber(String ticketNumber);
    
    /**
     * Get all tickets for a parking lot
     */
    List<ParkingTicketDTO> getTicketsByParkingLotId(Long parkingLotId);
    
    /**
     * Get active tickets by license plate
     */
    List<ParkingTicketDTO> getActiveTicketsByLicensePlate(String licensePlate);
    
    /**
     * Check if vehicle is currently parked
     */
    boolean isVehicleParked(String licensePlate);
    
    /**
     * Find best available spot for vehicle
     */
    Long findBestAvailableSpot(Long parkingLotId, String vehicleType);
} 