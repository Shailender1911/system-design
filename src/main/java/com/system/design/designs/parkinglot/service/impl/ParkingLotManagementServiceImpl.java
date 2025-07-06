package com.system.design.designs.parkinglot.service.impl;

import com.system.design.common.exception.BusinessException;
import com.system.design.common.exception.ResourceNotFoundException;
import com.system.design.designs.parkinglot.dto.CreateParkingLotRequest;
import com.system.design.designs.parkinglot.dto.ParkingLotDTO;
import com.system.design.designs.parkinglot.entity.ParkingFloor;
import com.system.design.designs.parkinglot.entity.ParkingLot;
import com.system.design.designs.parkinglot.entity.ParkingSpot;
import com.system.design.designs.parkinglot.repository.ParkingLotRepository;
import com.system.design.designs.parkinglot.repository.ParkingSpotRepository;
import com.system.design.designs.parkinglot.service.ParkingLotManagementService;
import com.system.design.designs.parkinglot.service.factory.ParkingSpotFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Implementation of ParkingLotManagementService
 * Follows Single Responsibility Principle - only manages parking lots
 * 
 * @author Shailender Kumar
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ParkingLotManagementServiceImpl implements ParkingLotManagementService {
    
    private final ParkingLotRepository parkingLotRepository;
    private final ParkingSpotRepository parkingSpotRepository;
    private final ParkingSpotFactory parkingSpotFactory;
    
    @Override
    @Transactional
    public ParkingLotDTO createParkingLot(CreateParkingLotRequest request) {
        log.info("Creating parking lot with name: {}", request.getName());
        
        // Check if parking lot with same name already exists
        if (parkingLotRepository.findByName(request.getName()).isPresent()) {
            throw new BusinessException("Parking lot with name '" + request.getName() + "' already exists");
        }
        
        // Create parking lot entity
        ParkingLot parkingLot = createParkingLotEntity(request);
        parkingLot = parkingLotRepository.save(parkingLot);
        
        // Create floors and spots using factory
        createFloorsAndSpots(parkingLot);
        
        log.info("Successfully created parking lot with ID: {}", parkingLot.getId());
        return convertToParkingLotDTO(parkingLot);
    }
    
    @Override
    @Transactional(readOnly = true)
    public ParkingLotDTO getParkingLotById(Long id) {
        log.info("Fetching parking lot with ID: {}", id);
        
        ParkingLot parkingLot = findParkingLotById(id);
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
    public ParkingLotDTO updateParkingLot(Long id, CreateParkingLotRequest request) {
        log.info("Updating parking lot with ID: {}", id);
        
        ParkingLot parkingLot = findParkingLotById(id);
        
        // Update basic information
        parkingLot.setName(request.getName());
        parkingLot.setLocation(request.getLocation());
        
        // Note: Floor and spot changes would require more complex logic
        // This is a simplified update for basic information only
        
        parkingLot = parkingLotRepository.save(parkingLot);
        
        log.info("Successfully updated parking lot with ID: {}", id);
        return convertToParkingLotDTO(parkingLot);
    }
    
    @Override
    @Transactional
    public void deactivateParkingLot(Long id) {
        log.info("Deactivating parking lot with ID: {}", id);
        
        ParkingLot parkingLot = findParkingLotById(id);
        
        // Check if there are any active tickets
        if (parkingLot.getAvailableSpots() < parkingLot.getTotalSpots()) {
            throw new BusinessException("Cannot deactivate parking lot with active vehicles");
        }
        
        parkingLot.setIsActive(false);
        parkingLotRepository.save(parkingLot);
        
        log.info("Successfully deactivated parking lot with ID: {}", id);
    }
    
    @Override
    @Transactional(readOnly = true)
    public ParkingLotDTO getParkingLotStatistics(Long id) {
        log.info("Fetching statistics for parking lot with ID: {}", id);
        
        ParkingLot parkingLot = findParkingLotById(id);
        ParkingLotDTO dto = convertToParkingLotDTO(parkingLot);
        
        // Add additional statistics here if needed
        log.info("Parking lot {} occupancy: {:.2f}%", 
                parkingLot.getName(), dto.getOccupancyPercentage());
        
        return dto;
    }
    
    /**
     * Create parking lot entity from request
     */
    private ParkingLot createParkingLotEntity(CreateParkingLotRequest request) {
        return ParkingLot.builder()
                .name(request.getName())
                .location(request.getLocation())
                .totalFloors(request.getTotalFloors())
                .spotsPerFloor(request.getSpotsPerFloor())
                .totalSpots(request.getTotalFloors() * request.getSpotsPerFloor())
                .availableSpots(request.getTotalFloors() * request.getSpotsPerFloor())
                .isActive(true)
                .build();
    }
    
    /**
     * Create floors and spots for parking lot
     */
    private void createFloorsAndSpots(ParkingLot parkingLot) {
        for (int floorNum = 1; floorNum <= parkingLot.getTotalFloors(); floorNum++) {
            ParkingFloor floor = ParkingFloor.builder()
                    .floorNumber(floorNum)
                    .totalSpots(parkingLot.getSpotsPerFloor())
                    .availableSpots(parkingLot.getSpotsPerFloor())
                    .parkingLot(parkingLot)
                    .build();
            
            // Create spots using factory
            List<ParkingSpot> spots = parkingSpotFactory.createSpotsForFloor(floor, parkingLot.getSpotsPerFloor());
            parkingSpotRepository.saveAll(spots);
        }
    }
    
    /**
     * Find parking lot by ID or throw exception
     */
    private ParkingLot findParkingLotById(Long id) {
        return parkingLotRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Parking lot not found with ID: " + id));
    }
    
    /**
     * Convert entity to DTO
     */
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
} 