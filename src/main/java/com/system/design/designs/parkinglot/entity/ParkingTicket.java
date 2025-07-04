package com.system.design.designs.parkinglot.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * ParkingTicket Entity representing a parking ticket
 * 
 * @author Shailender Kumar
 */
@Entity
@Table(name = "parking_ticket")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ParkingTicket {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "ticket_number", nullable = false, unique = true)
    private String ticketNumber;
    
    @Column(name = "license_plate", nullable = false)
    private String licensePlate;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "vehicle_type", nullable = false)
    private VehicleType vehicleType;
    
    @Column(name = "entry_time", nullable = false)
    private LocalDateTime entryTime;
    
    @Column(name = "exit_time")
    private LocalDateTime exitTime;
    
    @Column(name = "amount_paid", precision = 10, scale = 2)
    private BigDecimal amountPaid;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "payment_status", nullable = false)
    private PaymentStatus paymentStatus;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "ticket_status", nullable = false)
    private TicketStatus ticketStatus;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parking_lot_id", nullable = false)
    private ParkingLot parkingLot;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parking_spot_id", nullable = false)
    private ParkingSpot parkingSpot;
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
    
    /**
     * Check if ticket is active
     */
    public boolean isActive() {
        return ticketStatus == TicketStatus.ACTIVE;
    }
    
    /**
     * Check if ticket is paid
     */
    public boolean isPaid() {
        return paymentStatus == PaymentStatus.PAID;
    }
    
    /**
     * Complete the ticket (mark as exited)
     */
    public void complete() {
        this.ticketStatus = TicketStatus.COMPLETED;
        this.exitTime = LocalDateTime.now();
    }
} 