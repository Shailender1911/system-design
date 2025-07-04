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
 * ParkingSpot Entity representing individual parking spots
 * 
 * @author Shailender Kumar
 */
@Entity
@Table(name = "parking_spot")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ParkingSpot {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "spot_number", nullable = false)
    private String spotNumber;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "spot_type", nullable = false)
    private SpotType spotType;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "spot_status", nullable = false)
    private SpotStatus spotStatus;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parking_floor_id", nullable = false)
    private ParkingFloor parkingFloor;
    
    @OneToMany(mappedBy = "parkingSpot", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<ParkingTicket> tickets;
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
    
    /**
     * Check if spot is available
     */
    public boolean isAvailable() {
        return spotStatus == SpotStatus.AVAILABLE;
    }
    
    /**
     * Mark spot as occupied
     */
    public void occupy() {
        this.spotStatus = SpotStatus.OCCUPIED;
    }
    
    /**
     * Mark spot as available
     */
    public void release() {
        this.spotStatus = SpotStatus.AVAILABLE;
    }
    
    /**
     * Check if spot can accommodate vehicle type
     */
    public boolean canAccommodateVehicle(VehicleType vehicleType) {
        return switch (this.spotType) {
            case COMPACT -> vehicleType == VehicleType.MOTORCYCLE || vehicleType == VehicleType.CAR;
            case LARGE -> vehicleType == VehicleType.MOTORCYCLE || vehicleType == VehicleType.CAR || vehicleType == VehicleType.TRUCK;
            case MOTORCYCLE -> vehicleType == VehicleType.MOTORCYCLE;
            case HANDICAPPED -> vehicleType == VehicleType.CAR;
        };
    }
} 