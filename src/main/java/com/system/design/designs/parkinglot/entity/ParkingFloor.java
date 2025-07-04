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
 * ParkingFloor Entity representing a floor in the parking lot
 * 
 * @author Shailender Kumar
 */
@Entity
@Table(name = "parking_floor")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ParkingFloor {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "floor_number", nullable = false)
    private Integer floorNumber;
    
    @Column(name = "total_spots", nullable = false)
    private Integer totalSpots;
    
    @Column(name = "available_spots", nullable = false)
    private Integer availableSpots;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parking_lot_id", nullable = false)
    private ParkingLot parkingLot;
    
    @OneToMany(mappedBy = "parkingFloor", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<ParkingSpot> spots;
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
    
    /**
     * Check if floor has available spots
     */
    public boolean hasAvailableSpots() {
        return availableSpots > 0;
    }
} 