package com.system.design.designs.parkinglot.repository;

import com.system.design.designs.parkinglot.entity.ParkingTicket;
import com.system.design.designs.parkinglot.entity.PaymentStatus;
import com.system.design.designs.parkinglot.entity.TicketStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository interface for ParkingTicket entity
 * 
 * @author Shailender Kumar
 */
@Repository
public interface ParkingTicketRepository extends JpaRepository<ParkingTicket, Long> {
    
    /**
     * Find ticket by ticket number
     */
    Optional<ParkingTicket> findByTicketNumber(String ticketNumber);
    
    /**
     * Find active tickets by license plate
     */
    @Query("SELECT pt FROM ParkingTicket pt WHERE pt.licensePlate = :licensePlate AND pt.ticketStatus = :status")
    List<ParkingTicket> findActiveTicketsByLicensePlate(@Param("licensePlate") String licensePlate, @Param("status") TicketStatus status);
    
    /**
     * Find tickets by parking lot ID
     */
    List<ParkingTicket> findByParkingLotId(Long parkingLotId);
    
    /**
     * Find tickets by parking lot ID and status
     */
    @Query("SELECT pt FROM ParkingTicket pt WHERE pt.parkingLot.id = :parkingLotId AND pt.ticketStatus = :status")
    List<ParkingTicket> findByParkingLotIdAndStatus(@Param("parkingLotId") Long parkingLotId, @Param("status") TicketStatus status);
    
    /**
     * Find tickets by payment status
     */
    @Query("SELECT pt FROM ParkingTicket pt WHERE pt.paymentStatus = :paymentStatus")
    List<ParkingTicket> findByPaymentStatus(@Param("paymentStatus") PaymentStatus paymentStatus);
    
    /**
     * Find tickets by date range
     */
    @Query("SELECT pt FROM ParkingTicket pt WHERE pt.entryTime >= :startDate AND pt.entryTime <= :endDate")
    List<ParkingTicket> findByDateRange(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);
    
    /**
     * Count active tickets by parking lot ID
     */
    @Query("SELECT COUNT(pt) FROM ParkingTicket pt WHERE pt.parkingLot.id = :parkingLotId AND pt.ticketStatus = :status")
    Long countActiveTicketsByParkingLotId(@Param("parkingLotId") Long parkingLotId, @Param("status") TicketStatus status);
    
    /**
     * Find overdue tickets (parked for more than specified hours)
     */
    @Query("SELECT pt FROM ParkingTicket pt WHERE pt.ticketStatus = :status AND pt.entryTime < :cutoffTime")
    List<ParkingTicket> findOverdueTickets(@Param("status") TicketStatus status, @Param("cutoffTime") LocalDateTime cutoffTime);
} 