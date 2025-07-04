package com.system.design.designs.parkinglot.service;

import com.system.design.designs.parkinglot.dto.CreateParkingLotRequest;
import com.system.design.designs.parkinglot.dto.ParkVehicleRequest;
import com.system.design.designs.parkinglot.dto.ParkingLotDTO;
import com.system.design.designs.parkinglot.dto.ParkingTicketDTO;

import java.math.BigDecimal;
import java.util.List;

/**
 * Service interface for Parking Lot operations
 * 
 * @author Shailender Kumar
 */
public interface ParkingLotService {
    
    /**
     * Create a new parking lot
     */
    ParkingLotDTO createParkingLot(CreateParkingLotRequest request);
    
    /**
     * Get parking lot by ID
     */
    ParkingLotDTO getParkingLotById(Long id);
    
    /**
     * Get all parking lots
     */
    List<ParkingLotDTO> getAllParkingLots();
    
    /**
     * Get parking lots with available spots
     */
    List<ParkingLotDTO> getParkingLotsWithAvailableSpots();
    
    /**
     * Park a vehicle
     */
    ParkingTicketDTO parkVehicle(ParkVehicleRequest request);
    
    /**
     * Exit vehicle and calculate payment
     */
    ParkingTicketDTO exitVehicle(String ticketNumber);
    
    /**
     * Process payment for a ticket
     */
    ParkingTicketDTO processPayment(String ticketNumber, BigDecimal amount);
    
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
     * Calculate parking fee
     */
    BigDecimal calculateParkingFee(String ticketNumber);
} 