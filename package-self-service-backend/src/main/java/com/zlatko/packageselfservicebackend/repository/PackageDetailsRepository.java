package com.zlatko.packageselfservicebackend.repository;

import com.zlatko.packageselfservicebackend.model.entity.PackageDetailEntity;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PackageDetailsRepository extends JpaRepository<PackageDetailEntity, UUID> {
}
