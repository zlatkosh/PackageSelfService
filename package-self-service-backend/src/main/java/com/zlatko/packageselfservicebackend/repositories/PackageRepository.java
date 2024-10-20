package com.zlatko.packageselfservicebackend.repositories;

import com.zlatko.packageselfservicebackend.model.entities.EmployeeEntity;
import com.zlatko.packageselfservicebackend.model.entities.PackageEntity;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface PackageRepository extends JpaRepository<PackageEntity, UUID> {
    Optional<PackageEntity> findByIdAndSender(@NotNull UUID id, EmployeeEntity sender);
    List<PackageEntity> findBySender(EmployeeEntity sender);
}