package com.system.design.designs.parkinglot.controller;

import com.system.design.common.dto.ApiResponse;
import com.system.design.designs.parkinglot.dto.CreateParkingLotRequest;
import com.system.design.designs.parkinglot.dto.ParkVehicleRequest;
import com.system.design.designs.parkinglot.dto.ParkingLotDTO;
import com.system.design.designs.parkinglot.dto.ParkingTicketDTO;
import com.system.design.designs.parkinglot.service.ParkingLotService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

/**
 * REST Controller for Parking Lot Management System
 * 
 * @author Shailender Kumar
 */
@RestController
@RequestMapping("/api/parking-lot")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Parking Lot Management", description = "APIs for managing parking lots, vehicles, and tickets")
public class ParkingLotController {
    
    private final ParkingLotService parkingLotService;
    
    /**
     * Create a new parking lot
     */
    @PostMapping
    @Operation(summary = "Create a new parking lot", description = "Creates a new parking lot with specified floors and spots")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<ParkingLotDTO>> createParkingLot(
            @Valid @RequestBody CreateParkingLotRequest request) {
        
        log.info("Creating parking lot: {}", request.getName());
        
        ParkingLotDTO parkingLot = parkingLotService.createParkingLot(request);
        
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Parking lot created successfully", parkingLot));
    }
    
    /**
     * Get parking lot by ID
     */
    @GetMapping("/{id}")
    @Operation(summary = "Get parking lot by ID", description = "Retrieves parking lot details by ID")
    public ResponseEntity<ApiResponse<ParkingLotDTO>> getParkingLotById(
            @Parameter(description = "Parking lot ID") @PathVariable Long id) {
        
        log.info("Fetching parking lot with ID: {}", id);
        
        ParkingLotDTO parkingLot = parkingLotService.getParkingLotById(id);
        
        return ResponseEntity.ok(ApiResponse.success(parkingLot));
    }
    
    /**
     * Get all parking lots
     */
    @GetMapping
    @Operation(summary = "Get all parking lots", description = "Retrieves all active parking lots")
    public ResponseEntity<ApiResponse<List<ParkingLotDTO>>> getAllParkingLots() {
        
        log.info("Fetching all parking lots");
        
        List<ParkingLotDTO> parkingLots = parkingLotService.getAllParkingLots();
        
        return ResponseEntity.ok(ApiResponse.success("Parking lots retrieved successfully", parkingLots));
    }
    
    /**
     * Get parking lots with available spots
     */
    @GetMapping("/available")
    @Operation(summary = "Get available parking lots", description = "Retrieves parking lots with available spots")
    public ResponseEntity<ApiResponse<List<ParkingLotDTO>>> getAvailableParkingLots() {
        
        log.info("Fetching available parking lots");
        
        List<ParkingLotDTO> parkingLots = parkingLotService.getParkingLotsWithAvailableSpots();
        
        return ResponseEntity.ok(ApiResponse.success("Available parking lots retrieved successfully", parkingLots));
    }
    
    /**
     * Park a vehicle
     */
    @PostMapping("/park")
    @Operation(summary = "Park a vehicle", description = "Parks a vehicle and issues a parking ticket")
    public ResponseEntity<ApiResponse<ParkingTicketDTO>> parkVehicle(
            @Valid @RequestBody ParkVehicleRequest request) {
        
        log.info("Parking vehicle: {}", request.getLicensePlate());
        
        ParkingTicketDTO ticket = parkingLotService.parkVehicle(request);
        
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Vehicle parked successfully", ticket));
    }
    
    /**
     * Exit vehicle
     */
    @PostMapping("/exit/{ticketNumber}")
    @Operation(summary = "Exit vehicle", description = "Processes vehicle exit and calculates payment")
    public ResponseEntity<ApiResponse<ParkingTicketDTO>> exitVehicle(
            @Parameter(description = "Ticket number") @PathVariable String ticketNumber) {
        
        log.info("Processing exit for ticket: {}", ticketNumber);
        
        ParkingTicketDTO ticket = parkingLotService.exitVehicle(ticketNumber);
        
        return ResponseEntity.ok(ApiResponse.success("Vehicle exit processed successfully", ticket));
    }
    
    /**
     * Process payment
     */
    @PostMapping("/payment/{ticketNumber}")
    @Operation(summary = "Process payment", description = "Processes payment for a parking ticket")
    public ResponseEntity<ApiResponse<ParkingTicketDTO>> processPayment(
            @Parameter(description = "Ticket number") @PathVariable String ticketNumber,
            @Parameter(description = "Payment amount") @RequestParam BigDecimal amount) {
        
        log.info("Processing payment for ticket: {} with amount: {}", ticketNumber, amount);
        
        ParkingTicketDTO ticket = parkingLotService.processPayment(ticketNumber, amount);
        
        return ResponseEntity.ok(ApiResponse.success("Payment processed successfully", ticket));
    }
    
    /**
     * Get ticket by ticket number
     */
    @GetMapping("/ticket/{ticketNumber}")
    @Operation(summary = "Get ticket by number", description = "Retrieves ticket details by ticket number")
    public ResponseEntity<ApiResponse<ParkingTicketDTO>> getTicket(
            @Parameter(description = "Ticket number") @PathVariable String ticketNumber) {
        
        log.info("Fetching ticket: {}", ticketNumber);
        
        ParkingTicketDTO ticket = parkingLotService.getTicketByTicketNumber(ticketNumber);
        
        return ResponseEntity.ok(ApiResponse.success(ticket));
    }
    
    /**
     * Get tickets by parking lot ID
     */
    @GetMapping("/{parkingLotId}/tickets")
    @Operation(summary = "Get tickets by parking lot", description = "Retrieves all tickets for a specific parking lot")
    public ResponseEntity<ApiResponse<List<ParkingTicketDTO>>> getTicketsByParkingLot(
            @Parameter(description = "Parking lot ID") @PathVariable Long parkingLotId) {
        
        log.info("Fetching tickets for parking lot: {}", parkingLotId);
        
        List<ParkingTicketDTO> tickets = parkingLotService.getTicketsByParkingLotId(parkingLotId);
        
        return ResponseEntity.ok(ApiResponse.success("Tickets retrieved successfully", tickets));
    }
    
    /**
     * Get active tickets by license plate
     */
    @GetMapping("/tickets/license/{licensePlate}")
    @Operation(summary = "Get active tickets by license plate", description = "Retrieves active tickets for a specific license plate")
    public ResponseEntity<ApiResponse<List<ParkingTicketDTO>>> getActiveTicketsByLicensePlate(
            @Parameter(description = "License plate") @PathVariable String licensePlate) {
        
        log.info("Fetching active tickets for license plate: {}", licensePlate);
        
        List<ParkingTicketDTO> tickets = parkingLotService.getActiveTicketsByLicensePlate(licensePlate);
        
        return ResponseEntity.ok(ApiResponse.success("Active tickets retrieved successfully", tickets));
    }
    
    /**
     * Calculate parking fee
     */
    @GetMapping("/fee/{ticketNumber}")
    @Operation(summary = "Calculate parking fee", description = "Calculates the current parking fee for a ticket")
    public ResponseEntity<ApiResponse<BigDecimal>> calculateParkingFee(
            @Parameter(description = "Ticket number") @PathVariable String ticketNumber) {
        
        log.info("Calculating fee for ticket: {}", ticketNumber);
        
        BigDecimal fee = parkingLotService.calculateParkingFee(ticketNumber);
        
        return ResponseEntity.ok(ApiResponse.success("Parking fee calculated successfully", fee));
    }
    
    /**
     * Health check endpoint
     */
    @GetMapping("/health")
    @Operation(summary = "Health check", description = "Checks if the parking lot service is running")
    public ResponseEntity<ApiResponse<String>> healthCheck() {
        
        return ResponseEntity.ok(ApiResponse.success("Parking Lot Management System is running"));
    }
} 