package com.inventory.repository;

import com.inventory.model.Equipment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface EquipmentRepository extends JpaRepository<Equipment, Long> {

    List<Equipment> findByActiveTrueOrderByNameAsc();

    Optional<Equipment> findByIdAndActiveTrue(Long id);

    boolean existsBySerialNumberIgnoreCaseAndActiveTrue(String serialNumber);

    boolean existsBySerialNumberIgnoreCaseAndActiveTrueAndIdNot(String serialNumber, Long id);
}
