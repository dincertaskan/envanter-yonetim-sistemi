package com.inventory.repository;

import com.inventory.model.Reservation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    List<Reservation> findAllByOrderByStartTimeAsc();

    /**
     * Aynı ekipman için [start, end) aralığıyla çakışan bir rezervasyon var mı?
     * İki aralık, biri diğeri bitmeden başlıyorsa çakışır.
     */
    @Query("""
            SELECT COUNT(r) > 0 FROM Reservation r
            WHERE r.equipment.id = :equipmentId
              AND r.startTime < :end
              AND r.endTime > :start
            """)
    boolean existsOverlapping(@Param("equipmentId") Long equipmentId,
                              @Param("start") LocalDateTime start,
                              @Param("end") LocalDateTime end);

    void deleteByEquipmentIdAndStartTimeAfter(Long equipmentId, LocalDateTime time);
}
