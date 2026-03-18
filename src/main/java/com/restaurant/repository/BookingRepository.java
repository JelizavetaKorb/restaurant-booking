package com.restaurant.repository;
import com.restaurant.model.Booking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {
    
    @Query("SELECT b FROM Booking b WHERE b.date = :date ORDER BY b.date, b.startTime")
    List<Booking> findByDate(@Param("date") LocalDate date);
    
    List<Booking> findByTableId(Long tableId);
    
    List<Booking> findByGroupId(String groupId);
    
    void deleteByGroupId(String groupId);
    
    @Query("SELECT b FROM Booking b WHERE b.tableId = :tableId AND b.date = :date " +
           "AND ((b.startTime <= :endTime AND b.endTime > :startTime))")
    List<Booking> findConflictingBookings(@Param("tableId") Long tableId, 
                                        @Param("date") LocalDate date,
                                        @Param("startTime") LocalTime startTime, 
                                        @Param("endTime") LocalTime endTime);
    
    @Query("SELECT b FROM Booking b WHERE b.date >= :date ORDER BY b.date, b.startTime")
    List<Booking> findUpcomingBookings(@Param("date") LocalDate date);
}
