package com.smartrental.backend.repository;

import com.smartrental.backend.entity.AmenitiesRef;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AmenitiesRepository extends JpaRepository<AmenitiesRef, Integer> {
    boolean existsByName(String name);
}