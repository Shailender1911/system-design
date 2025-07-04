package com.system.design.designs.parkinglot.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.List;

/**
 * ParkingLot Entity representing a parking lot with multiple floors and spots
 * 
 * @author Shailender Kumar
 */
@Entity
@Table(name = "parking_lot")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ParkingLot {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, unique = true)
    private String name;
    
    @Column(nullable = false)
    private String location;
    
    @Column(name = "total_floors", nullable = false)
    private Integer totalFloors;
    
    @Column(name = "spots_per_floor", nullable = false)
    private Integer spotsPerFloor;
    
    @Column(name = "total_spots", nullable = false)
    private Integer totalSpots;
    
    @Column(name = "available_spots", nullable = false)
    private Integer availableSpots;
    
    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;
    
    @OneToMany(mappedBy = "parkingLot", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<ParkingFloor> floors;
    
    @OneToMany(mappedBy = "parkingLot", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<ParkingTicket> tickets;
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
    
    /**
     * Check if parking lot has available spots
     */
    public boolean hasAvailableSpots() {
        return availableSpots > 0;
    }
    
    /**
     * Reserve a spot (decrement available spots)
     */
    public void reserveSpot() {
        if (availableSpots > 0) {
            availableSpots--;
        }
    }
    
    /**
     * Release a spot (increment available spots)
     */
    public void releaseSpot() {
        if (availableSpots < totalSpots) {
            availableSpots++;
        }
    }
} 