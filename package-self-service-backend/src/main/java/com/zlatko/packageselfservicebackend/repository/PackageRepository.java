package com.zlatko.packageselfservicebackend.repository;

import com.zlatko.packageselfservicebackend.model.entity.PackageEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface PackageRepository extends JpaRepository<PackageEntity, UUID> {
}