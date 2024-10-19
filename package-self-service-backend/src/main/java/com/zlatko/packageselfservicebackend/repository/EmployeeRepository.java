package com.zlatko.packageselfservicebackend.repository;

import com.zlatko.packageselfservicebackend.model.entity.EmployeeEntity;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EmployeeRepository extends JpaRepository<EmployeeEntity, UUID> {
}
