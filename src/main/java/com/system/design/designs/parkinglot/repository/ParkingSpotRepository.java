package com.system.design.designs.parkinglot.repository;

import com.system.design.designs.parkinglot.entity.ParkingSpot;
import com.system.design.designs.parkinglot.entity.SpotStatus;
import com.system.design.designs.parkinglot.entity.SpotType;
import com.system.design.designs.parkinglot.entity.VehicleType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for ParkingSpot entity
 * 
 * @author Shailender Kumar
 */
@Repository
public interface ParkingSpotRepository extends JpaRepository<ParkingSpot, Long> {
    
    /**
     * Find available spots by parking lot ID
     */
    @Query("SELECT ps FROM ParkingSpot ps WHERE ps.parkingFloor.parkingLot.id = :parkingLotId AND ps.spotStatus = :status")
    List<ParkingSpot> findAvailableSpotsByParkingLotId(@Param("parkingLotId") Long parkingLotId, @Param("status") SpotStatus status);
    
    /**
     * Find spot by spot number and parking lot ID
     */
    @Query("SELECT ps FROM ParkingSpot ps WHERE ps.spotNumber = :spotNumber AND ps.parkingFloor.parkingLot.id = :parkingLotId")
    Optional<ParkingSpot> findBySpotNumberAndParkingLotId(@Param("spotNumber") String spotNumber, @Param("parkingLotId") Long parkingLotId);
    
    /**
     * Find available spots by spot type and parking lot ID
     */
    @Query("SELECT ps FROM ParkingSpot ps WHERE ps.parkingFloor.parkingLot.id = :parkingLotId AND ps.spotType = :spotType AND ps.spotStatus = :status")
    List<ParkingSpot> findAvailableSpotsByTypeAndParkingLotId(
            @Param("parkingLotId") Long parkingLotId, 
            @Param("spotType") SpotType spotType, 
            @Param("status") SpotStatus status);
    
    /**
     * Find the first available spot for a vehicle type
     */
    @Query("SELECT ps FROM ParkingSpot ps WHERE ps.parkingFloor.parkingLot.id = :parkingLotId AND ps.spotStatus = :status AND " +
           "((ps.spotType = 'MOTORCYCLE' AND :vehicleType = 'MOTORCYCLE') OR " +
           "(ps.spotType = 'COMPACT' AND :vehicleType IN ('MOTORCYCLE', 'CAR')) OR " +
           "(ps.spotType = 'LARGE' AND :vehicleType IN ('MOTORCYCLE', 'CAR', 'TRUCK')) OR " +
           "(ps.spotType = 'HANDICAPPED' AND :vehicleType = 'CAR')) " +
           "ORDER BY ps.parkingFloor.floorNumber, ps.spotNumber")
    Optional<ParkingSpot> findFirstAvailableSpotForVehicleType(
            @Param("parkingLotId") Long parkingLotId, 
            @Param("vehicleType") VehicleType vehicleType, 
            @Param("status") SpotStatus status);
    
    /**
     * Count available spots by parking lot ID
     */
    @Query("SELECT COUNT(ps) FROM ParkingSpot ps WHERE ps.parkingFloor.parkingLot.id = :parkingLotId AND ps.spotStatus = :status")
    Long countAvailableSpotsByParkingLotId(@Param("parkingLotId") Long parkingLotId, @Param("status") SpotStatus status);
} 