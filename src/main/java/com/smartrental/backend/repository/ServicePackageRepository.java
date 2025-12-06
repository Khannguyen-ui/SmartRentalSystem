package com.smartrental.backend.repository;

import com.smartrental.backend.entity.ServicePackage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ServicePackageRepository extends JpaRepository<ServicePackage, Integer> {
}