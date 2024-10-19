package com.zlatko.packageselfservicebackend.repositories;

import com.zlatko.packageselfservicebackend.model.entities.PackageEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface PackageRepository extends JpaRepository<PackageEntity, UUID> {
}