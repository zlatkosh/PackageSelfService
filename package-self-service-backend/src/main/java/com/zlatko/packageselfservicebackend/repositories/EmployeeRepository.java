package com.zlatko.packageselfservicebackend.repositories;

import com.zlatko.packageselfservicebackend.model.entities.EmployeeEntity;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EmployeeRepository extends JpaRepository<EmployeeEntity, UUID> {
}
