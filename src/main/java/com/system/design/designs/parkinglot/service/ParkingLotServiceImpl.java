package com.system.design.designs.parkinglot.service;

import com.system.design.common.exception.BusinessException;
import com.system.design.common.exception.ResourceNotFoundException;
import com.system.design.designs.parkinglot.dto.CreateParkingLotRequest;
import com.system.design.designs.parkinglot.dto.ParkVehicleRequest;
import com.system.design.designs.parkinglot.dto.ParkingLotDTO;
import com.system.design.designs.parkinglot.dto.ParkingTicketDTO;
import com.system.design.designs.parkinglot.entity.*;
import com.system.design.designs.parkinglot.repository.ParkingLotRepository;
import com.system.design.designs.parkinglot.repository.ParkingSpotRepository;
import com.system.design.designs.parkinglot.repository.ParkingTicketRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Implementation of ParkingLotService
 * 
 * @author Shailender Kumar
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ParkingLotServiceImpl implements ParkingLotService {
    
    private final ParkingLotRepository parkingLotRepository;
    private final ParkingSpotRepository parkingSpotRepository;
    private final ParkingTicketRepository parkingTicketRepository;
    
    // Pricing constants
    private static final BigDecimal HOURLY_RATE = BigDecimal.valueOf(5.0);
    private static final BigDecimal MINIMUM_CHARGE = BigDecimal.valueOf(2.0);
    private static final BigDecimal DAILY_RATE = BigDecimal.valueOf(25.0);
    private static final int HOURS_IN_DAY = 24;
    
    @Override
    @Transactional
    public ParkingLotDTO createParkingLot(CreateParkingLotRequest request) {
        log.info("Creating parking lot with name: {}", request.getName());
        
        // Check if parking lot with same name already exists
        if (parkingLotRepository.findByName(request.getName()).isPresent()) {
            throw new BusinessException("Parking lot with name '" + request.getName() + "' already exists");
        }
        
        // Create parking lot
        ParkingLot parkingLot = ParkingLot.builder()
                .name(request.getName())
                .location(request.getLocation())
                .totalFloors(request.getTotalFloors())
                .spotsPerFloor(request.getSpotsPerFloor())
                .totalSpots(request.getTotalFloors() * request.getSpotsPerFloor())
                .availableSpots(request.getTotalFloors() * request.getSpotsPerFloor())
                .isActive(true)
                .build();
        
        parkingLot = parkingLotRepository.save(parkingLot);
        
        // Create floors and spots
        createFloorsAndSpots(parkingLot);
        
        log.info("Successfully created parking lot with ID: {}", parkingLot.getId());
        return convertToParkingLotDTO(parkingLot);
    }
    
    @Override
    @Transactional(readOnly = true)
    public ParkingLotDTO getParkingLotById(Long id) {
        log.info("Fetching parking lot with ID: {}", id);
        
        ParkingLot parkingLot = parkingLotRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Parking lot not found with ID: " + id));
        
        return convertToParkingLotDTO(parkingLot);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<ParkingLotDTO> getAllParkingLots() {
        log.info("Fetching all parking lots");
        
        return parkingLotRepository.findAllActive().stream()
                .map(this::convertToParkingLotDTO)
                .collect(Collectors.toList());
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<ParkingLotDTO> getParkingLotsWithAvailableSpots() {
        log.info("Fetching parking lots with available spots");
        
        return parkingLotRepository.findAllWithAvailableSpots().stream()
                .map(this::convertToParkingLotDTO)
                .collect(Collectors.toList());
    }
    
    @Override
    @Transactional
    public ParkingTicketDTO parkVehicle(ParkVehicleRequest request) {
        log.info("Parking vehicle with license plate: {} at parking lot: {}", 
                request.getLicensePlate(), request.getParkingLotId());
        
        // Check if vehicle is already parked
        List<ParkingTicket> activeTickets = parkingTicketRepository
                .findActiveTicketsByLicensePlate(request.getLicensePlate(), TicketStatus.ACTIVE);
        
        if (!activeTickets.isEmpty()) {
            throw new BusinessException("Vehicle with license plate '" + request.getLicensePlate() + 
                                      "' is already parked");
        }
        
        // Find parking lot
        ParkingLot parkingLot = parkingLotRepository.findById(request.getParkingLotId())
                .orElseThrow(() -> new ResourceNotFoundException("Parking lot not found with ID: " + request.getParkingLotId()));
        
        if (!parkingLot.hasAvailableSpots()) {
            throw new BusinessException("No available spots in parking lot: " + parkingLot.getName());
        }
        
        // Find available spot
        ParkingSpot availableSpot = parkingSpotRepository
                .findFirstAvailableSpotForVehicleType(request.getParkingLotId(), request.getVehicleType(), SpotStatus.AVAILABLE)
                .orElseThrow(() -> new BusinessException("No suitable spot available for vehicle type: " + request.getVehicleType()));
        
        // Create parking ticket
        ParkingTicket ticket = ParkingTicket.builder()
                .ticketNumber(generateTicketNumber())
                .licensePlate(request.getLicensePlate())
                .vehicleType(request.getVehicleType())
                .entryTime(LocalDateTime.now())
                .paymentStatus(PaymentStatus.PENDING)
                .ticketStatus(TicketStatus.ACTIVE)
                .parkingLot(parkingLot)
                .parkingSpot(availableSpot)
                .build();
        
        ticket = parkingTicketRepository.save(ticket);
        
        // Update spot status
        availableSpot.occupy();
        parkingSpotRepository.save(availableSpot);
        
        // Update parking lot available spots
        parkingLot.reserveSpot();
        parkingLotRepository.save(parkingLot);
        
        log.info("Successfully parked vehicle. Ticket number: {}", ticket.getTicketNumber());
        return convertToParkingTicketDTO(ticket);
    }
    
    @Override
    @Transactional
    public ParkingTicketDTO exitVehicle(String ticketNumber) {
        log.info("Processing exit for ticket: {}", ticketNumber);
        
        ParkingTicket ticket = parkingTicketRepository.findByTicketNumber(ticketNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket not found with number: " + ticketNumber));
        
        if (ticket.getTicketStatus() != TicketStatus.ACTIVE) {
            throw new BusinessException("Ticket is not active");
        }
        
        // Calculate fee
        BigDecimal fee = calculateParkingFee(ticketNumber);
        
        // Complete ticket
        ticket.complete();
        ticket.setAmountPaid(fee);
        ticket.setPaymentStatus(PaymentStatus.PAID);
        
        ticket = parkingTicketRepository.save(ticket);
        
        // Release spot
        ParkingSpot spot = ticket.getParkingSpot();
        spot.release();
        parkingSpotRepository.save(spot);
        
        // Update parking lot available spots
        ParkingLot parkingLot = ticket.getParkingLot();
        parkingLot.releaseSpot();
        parkingLotRepository.save(parkingLot);
        
        log.info("Successfully processed exit for ticket: {}. Fee: {}", ticketNumber, fee);
        return convertToParkingTicketDTO(ticket);
    }
    
    @Override
    @Transactional
    public ParkingTicketDTO processPayment(String ticketNumber, BigDecimal amount) {
        log.info("Processing payment for ticket: {}. Amount: {}", ticketNumber, amount);
        
        ParkingTicket ticket = parkingTicketRepository.findByTicketNumber(ticketNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket not found with number: " + ticketNumber));
        
        BigDecimal requiredAmount = calculateParkingFee(ticketNumber);
        
        if (amount.compareTo(requiredAmount) < 0) {
            throw new BusinessException("Insufficient payment amount. Required: " + requiredAmount + ", Provided: " + amount);
        }
        
        ticket.setAmountPaid(amount);
        ticket.setPaymentStatus(PaymentStatus.PAID);
        
        ticket = parkingTicketRepository.save(ticket);
        
        log.info("Successfully processed payment for ticket: {}", ticketNumber);
        return convertToParkingTicketDTO(ticket);
    }
    
    @Override
    @Transactional(readOnly = true)
    public ParkingTicketDTO getTicketByTicketNumber(String ticketNumber) {
        log.info("Fetching ticket: {}", ticketNumber);
        
        ParkingTicket ticket = parkingTicketRepository.findByTicketNumber(ticketNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket not found with number: " + ticketNumber));
        
        return convertToParkingTicketDTO(ticket);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<ParkingTicketDTO> getTicketsByParkingLotId(Long parkingLotId) {
        log.info("Fetching tickets for parking lot: {}", parkingLotId);
        
        return parkingTicketRepository.findByParkingLotId(parkingLotId).stream()
                .map(this::convertToParkingTicketDTO)
                .collect(Collectors.toList());
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<ParkingTicketDTO> getActiveTicketsByLicensePlate(String licensePlate) {
        log.info("Fetching active tickets for license plate: {}", licensePlate);
        
        return parkingTicketRepository.findActiveTicketsByLicensePlate(licensePlate, TicketStatus.ACTIVE).stream()
                .map(this::convertToParkingTicketDTO)
                .collect(Collectors.toList());
    }
    
    @Override
    @Transactional(readOnly = true)
    public BigDecimal calculateParkingFee(String ticketNumber) {
        ParkingTicket ticket = parkingTicketRepository.findByTicketNumber(ticketNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket not found with number: " + ticketNumber));
        
        LocalDateTime endTime = ticket.getExitTime() != null ? ticket.getExitTime() : LocalDateTime.now();
        Duration duration = Duration.between(ticket.getEntryTime(), endTime);
        
        long hours = duration.toHours();
        if (duration.toMinutes() % 60 > 0) {
            hours++; // Round up to next hour
        }
        
        BigDecimal fee;
        if (hours >= HOURS_IN_DAY) {
            // Daily rate for 24+ hours
            long days = hours / HOURS_IN_DAY;
            long remainingHours = hours % HOURS_IN_DAY;
            fee = DAILY_RATE.multiply(BigDecimal.valueOf(days))
                    .add(HOURLY_RATE.multiply(BigDecimal.valueOf(remainingHours)));
        } else {
            // Hourly rate
            fee = HOURLY_RATE.multiply(BigDecimal.valueOf(hours));
        }
        
        // Apply minimum charge
        return fee.max(MINIMUM_CHARGE);
    }
    
    private void createFloorsAndSpots(ParkingLot parkingLot) {
        for (int floorNum = 1; floorNum <= parkingLot.getTotalFloors(); floorNum++) {
            ParkingFloor floor = ParkingFloor.builder()
                    .floorNumber(floorNum)
                    .totalSpots(parkingLot.getSpotsPerFloor())
                    .availableSpots(parkingLot.getSpotsPerFloor())
                    .parkingLot(parkingLot)
                    .build();
            
            // Create spots for this floor
            for (int spotNum = 1; spotNum <= parkingLot.getSpotsPerFloor(); spotNum++) {
                SpotType spotType = determineSpotType(spotNum, parkingLot.getSpotsPerFloor());
                
                ParkingSpot spot = ParkingSpot.builder()
                        .spotNumber(String.format("F%d-S%02d", floorNum, spotNum))
                        .spotType(spotType)
                        .spotStatus(SpotStatus.AVAILABLE)
                        .parkingFloor(floor)
                        .build();
                
                parkingSpotRepository.save(spot);
            }
        }
    }
    
    private SpotType determineSpotType(int spotNumber, int totalSpotsPerFloor) {
        // Distribute spot types: 20% motorcycle, 60% compact, 15% large, 5% handicapped
        double ratio = (double) spotNumber / totalSpotsPerFloor;
        
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
    
    private String generateTicketNumber() {
        return "TKT-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }
    
    private ParkingLotDTO convertToParkingLotDTO(ParkingLot parkingLot) {
        return ParkingLotDTO.builder()
                .id(parkingLot.getId())
                .name(parkingLot.getName())
                .location(parkingLot.getLocation())
                .totalFloors(parkingLot.getTotalFloors())
                .spotsPerFloor(parkingLot.getSpotsPerFloor())
                .totalSpots(parkingLot.getTotalSpots())
                .availableSpots(parkingLot.getAvailableSpots())
                .isActive(parkingLot.getIsActive())
                .createdAt(parkingLot.getCreatedAt())
                .updatedAt(parkingLot.getUpdatedAt())
                .build();
    }
    
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