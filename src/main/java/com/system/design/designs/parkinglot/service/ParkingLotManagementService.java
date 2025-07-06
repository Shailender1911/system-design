package com.system.design.designs.parkinglot.service;

import com.system.design.designs.parkinglot.dto.CreateParkingLotRequest;
import com.system.design.designs.parkinglot.dto.ParkingLotDTO;

import java.util.List;

/**
 * Service interface for parking lot management operations
 * Follows Single Responsibility Principle - only manages parking lots
 * 
 * @author Shailender Kumar
 */
public interface ParkingLotManagementService {
    
    /**
     * Create a new parking lot
     */
    ParkingLotDTO createParkingLot(CreateParkingLotRequest request);
    
    /**
     * Get parking lot by ID
     */
    ParkingLotDTO getParkingLotById(Long id);
    
    /**
     * Get all active parking lots
     */
    List<ParkingLotDTO> getAllParkingLots();
    
    /**
     * Get parking lots with available spots
     */
    List<ParkingLotDTO> getParkingLotsWithAvailableSpots();
    
    /**
     * Update parking lot details
     */
    ParkingLotDTO updateParkingLot(Long id, CreateParkingLotRequest request);
    
    /**
     * Deactivate parking lot
     */
    void deactivateParkingLot(Long id);
    
    /**
     * Get parking lot occupancy statistics
     */
    ParkingLotDTO getParkingLotStatistics(Long id);
} 