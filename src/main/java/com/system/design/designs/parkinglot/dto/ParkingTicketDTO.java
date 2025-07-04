package com.system.design.designs.parkinglot.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.system.design.designs.parkinglot.entity.PaymentStatus;
import com.system.design.designs.parkinglot.entity.TicketStatus;
import com.system.design.designs.parkinglot.entity.VehicleType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO for ParkingTicket entity
 * 
 * @author Shailender Kumar
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ParkingTicketDTO {
    
    private Long id;
    private String ticketNumber;
    private String licensePlate;
    private VehicleType vehicleType;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime entryTime;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime exitTime;
    
    private BigDecimal amountPaid;
    private PaymentStatus paymentStatus;
    private TicketStatus ticketStatus;
    
    private Long parkingLotId;
    private String parkingLotName;
    private String spotNumber;
    private Integer floorNumber;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedAt;
} 