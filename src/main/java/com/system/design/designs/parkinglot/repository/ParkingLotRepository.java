package com.system.design.designs.parkinglot.repository;

import com.system.design.designs.parkinglot.entity.ParkingLot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for ParkingLot entity
 * 
 * @author Shailender Kumar
 */
@Repository
public interface ParkingLotRepository extends JpaRepository<ParkingLot, Long> {
    
    /**
     * Find parking lot by name
     */
    Optional<ParkingLot> findByName(String name);
    
    /**
     * Find all active parking lots
     */
    @Query("SELECT pl FROM ParkingLot pl WHERE pl.isActive = true")
    List<ParkingLot> findAllActive();
    
    /**
     * Find parking lots by location
     */
    List<ParkingLot> findByLocationContainingIgnoreCase(String location);
    
    /**
     * Find parking lots with available spots
     */
    @Query("SELECT pl FROM ParkingLot pl WHERE pl.availableSpots > 0 AND pl.isActive = true")
    List<ParkingLot> findAllWithAvailableSpots();
    
    /**
     * Count total parking lots
     */
    @Query("SELECT COUNT(pl) FROM ParkingLot pl WHERE pl.isActive = true")
    Long countActiveParkingLots();
    
    /**
     * Find parking lots by minimum available spots
     */
    @Query("SELECT pl FROM ParkingLot pl WHERE pl.availableSpots >= :minSpots AND pl.isActive = true")
    List<ParkingLot> findByMinimumAvailableSpots(@Param("minSpots") Integer minSpots);
} 