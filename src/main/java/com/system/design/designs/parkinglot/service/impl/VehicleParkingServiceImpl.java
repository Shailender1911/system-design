package com.system.design.designs.parkinglot.service.impl;

import com.system.design.common.exception.BusinessException;
import com.system.design.common.exception.ResourceNotFoundException;
import com.system.design.designs.parkinglot.dto.ParkVehicleRequest;
import com.system.design.designs.parkinglot.dto.ParkingTicketDTO;
import com.system.design.designs.parkinglot.entity.*;
import com.system.design.designs.parkinglot.repository.ParkingLotRepository;
import com.system.design.designs.parkinglot.repository.ParkingSpotRepository;
import com.system.design.designs.parkinglot.repository.ParkingTicketRepository;
import com.system.design.designs.parkinglot.service.VehicleParkingService;
import com.system.design.designs.parkinglot.service.strategy.PricingContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Implementation of VehicleParkingService
 * Follows Single Responsibility Principle - only manages vehicle parking/exit
 * Uses Strategy Pattern for pricing calculations
 * 
 * @author Shailender Kumar
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class VehicleParkingServiceImpl implements VehicleParkingService {
    
    private final ParkingLotRepository parkingLotRepository;
    private final ParkingSpotRepository parkingSpotRepository;
    private final ParkingTicketRepository parkingTicketRepository;
    private final PricingContext pricingContext;
    
    @Override
    @Transactional
    public ParkingTicketDTO parkVehicle(ParkVehicleRequest request) {
        log.info("Parking vehicle with license plate: {} at parking lot: {}", 
                request.getLicensePlate(), request.getParkingLotId());
        
        // Check if vehicle is already parked
        if (isVehicleParked(request.getLicensePlate())) {
            throw new BusinessException("Vehicle with license plate '" + request.getLicensePlate() + 
                                      "' is already parked");
        }
        
        // Find parking lot
        ParkingLot parkingLot = findParkingLotById(request.getParkingLotId());
        
        if (!parkingLot.hasAvailableSpots()) {
            throw new BusinessException("No available spots in parking lot: " + parkingLot.getName());
        }
        
        // Find available spot
        ParkingSpot availableSpot = findAvailableSpot(request.getParkingLotId(), request.getVehicleType());
        
        // Create parking ticket
        ParkingTicket ticket = createParkingTicket(request, parkingLot, availableSpot);
        ticket = parkingTicketRepository.save(ticket);
        
        // Update spot and parking lot
        updateSpotAndParkingLot(availableSpot, parkingLot);
        
        log.info("Successfully parked vehicle. Ticket number: {}", ticket.getTicketNumber());
        return convertToParkingTicketDTO(ticket);
    }
    
    @Override
    @Transactional
    public ParkingTicketDTO exitVehicle(String ticketNumber) {
        log.info("Processing exit for ticket: {}", ticketNumber);
        
        ParkingTicket ticket = findTicketByNumber(ticketNumber);
        
        if (ticket.getTicketStatus() != TicketStatus.ACTIVE) {
            throw new BusinessException("Ticket is not active");
        }
        
        // Calculate fee using strategy pattern
        BigDecimal fee = calculateFee(ticket);
        
        // Complete ticket
        ticket.complete();
        ticket.setAmountPaid(fee);
        ticket.setPaymentStatus(PaymentStatus.PAID);
        
        ticket = parkingTicketRepository.save(ticket);
        
        // Release spot and update parking lot
        releaseSpotAndUpdateParkingLot(ticket);
        
        log.info("Successfully processed exit for ticket: {}. Fee: {}", ticketNumber, fee);
        return convertToParkingTicketDTO(ticket);
    }
    
    @Override
    @Transactional(readOnly = true)
    public ParkingTicketDTO getTicketByTicketNumber(String ticketNumber) {
        log.info("Fetching ticket: {}", ticketNumber);
        
        ParkingTicket ticket = findTicketByNumber(ticketNumber);
        return convertToParkingTicketDTO(ticket);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<ParkingTicketDTO> getTicketsByParkingLotId(Long parkingLotId) {
        log.info("Fetching tickets for parking lot: {}", parkingLotId);
        
        List<ParkingTicket> tickets = parkingTicketRepository.findByParkingLotId(parkingLotId);
        return tickets.stream()
                .map(this::convertToParkingTicketDTO)
                .collect(Collectors.toList());
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<ParkingTicketDTO> getActiveTicketsByLicensePlate(String licensePlate) {
        log.info("Fetching active tickets for license plate: {}", licensePlate);
        
        List<ParkingTicket> tickets = parkingTicketRepository
                .findActiveTicketsByLicensePlate(licensePlate, TicketStatus.ACTIVE);
        return tickets.stream()
                .map(this::convertToParkingTicketDTO)
                .collect(Collectors.toList());
    }
    
    @Override
    @Transactional(readOnly = true)
    public boolean isVehicleParked(String licensePlate) {
        List<ParkingTicket> activeTickets = parkingTicketRepository
                .findActiveTicketsByLicensePlate(licensePlate, TicketStatus.ACTIVE);
        return !activeTickets.isEmpty();
    }
    
    @Override
    @Transactional(readOnly = true)
    public Long findBestAvailableSpot(Long parkingLotId, String vehicleType) {
        log.info("Finding best available spot for vehicle type: {} in parking lot: {}", vehicleType, parkingLotId);
        
        VehicleType type = VehicleType.valueOf(vehicleType.toUpperCase());
        ParkingSpot spot = parkingSpotRepository
                .findFirstAvailableSpotForVehicleType(parkingLotId, type, SpotStatus.AVAILABLE)
                .orElse(null);
        
        return spot != null ? spot.getId() : null;
    }
    
    /**
     * Calculate parking fee using strategy pattern
     */
    private BigDecimal calculateFee(ParkingTicket ticket) {
        LocalDateTime exitTime = LocalDateTime.now();
        return pricingContext.calculateFee(ticket.getEntryTime(), exitTime, ticket.getVehicleType());
    }
    
    /**
     * Find parking lot by ID
     */
    private ParkingLot findParkingLotById(Long parkingLotId) {
        return parkingLotRepository.findById(parkingLotId)
                .orElseThrow(() -> new ResourceNotFoundException("Parking lot not found with ID: " + parkingLotId));
    }
    
    /**
     * Find available spot for vehicle type
     */
    private ParkingSpot findAvailableSpot(Long parkingLotId, VehicleType vehicleType) {
        return parkingSpotRepository
                .findFirstAvailableSpotForVehicleType(parkingLotId, vehicleType, SpotStatus.AVAILABLE)
                .orElseThrow(() -> new BusinessException("No suitable spot available for vehicle type: " + vehicleType));
    }
    
    /**
     * Find ticket by ticket number
     */
    private ParkingTicket findTicketByNumber(String ticketNumber) {
        return parkingTicketRepository.findByTicketNumber(ticketNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket not found with number: " + ticketNumber));
    }
    
    /**
     * Create parking ticket
     */
    private ParkingTicket createParkingTicket(ParkVehicleRequest request, ParkingLot parkingLot, ParkingSpot availableSpot) {
        return ParkingTicket.builder()
                .ticketNumber(generateTicketNumber())
                .licensePlate(request.getLicensePlate())
                .vehicleType(request.getVehicleType())
                .entryTime(LocalDateTime.now())
                .paymentStatus(PaymentStatus.PENDING)
                .ticketStatus(TicketStatus.ACTIVE)
                .parkingLot(parkingLot)
                .parkingSpot(availableSpot)
                .build();
    }
    
    /**
     * Update spot and parking lot after parking
     */
    private void updateSpotAndParkingLot(ParkingSpot spot, ParkingLot parkingLot) {
        spot.occupy();
        parkingSpotRepository.save(spot);
        
        parkingLot.reserveSpot();
        parkingLotRepository.save(parkingLot);
    }
    
    /**
     * Release spot and update parking lot after exit
     */
    private void releaseSpotAndUpdateParkingLot(ParkingTicket ticket) {
        ParkingSpot spot = ticket.getParkingSpot();
        spot.release();
        parkingSpotRepository.save(spot);
        
        ParkingLot parkingLot = ticket.getParkingLot();
        parkingLot.releaseSpot();
        parkingLotRepository.save(parkingLot);
    }
    
    /**
     * Generate unique ticket number
     */
    private String generateTicketNumber() {
        return "TKT-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }
    
    /**
     * Convert entity to DTO
     */
    private ParkingTicketDTO convertToParkingTicketDTO(ParkingTicket ticket) {
        return ParkingTicketDTO.builder()
                .id(ticket.getId())
                .ticketNumber(ticket.getTicketNumber())
                .licensePlate(ticket.getLicensePlate())
                .vehicleType(ticket.getVehicleType())
                .entryTime(ticket.getEntryTime())
                .exitTime(ticket.getExitTime())
                .amountPaid(ticket.getAmountPaid())
                .paymentStatus(ticket.getPaymentStatus())
                .ticketStatus(ticket.getTicketStatus())
                .parkingLotId(ticket.getParkingLot().getId())
                .parkingLotName(ticket.getParkingLot().getName())
                .spotNumber(ticket.getParkingSpot().getSpotNumber())
                .floorNumber(ticket.getParkingSpot().getParkingFloor().getFloorNumber())
                .createdAt(ticket.getCreatedAt())
                .updatedAt(ticket.getUpdatedAt())
                .build();
    }
} 