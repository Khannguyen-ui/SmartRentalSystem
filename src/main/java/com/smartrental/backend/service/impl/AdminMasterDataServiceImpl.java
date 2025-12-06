package com.smartrental.backend.service.impl;

import com.smartrental.backend.entity.AmenitiesRef;
import com.smartrental.backend.entity.ServicePackage;
import com.smartrental.backend.repository.AmenitiesRepository;
import com.smartrental.backend.repository.ServicePackageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminMasterDataServiceImpl {

    private final AmenitiesRepository amenitiesRepository;
    private final ServicePackageRepository packageRepository;

    // --- 1. QUẢN LÝ TIỆN ÍCH (AMENITIES) ---

    public List<AmenitiesRef> getAllAmenities() {
        return amenitiesRepository.findAll();
    }

    @Transactional
    public AmenitiesRef createAmenity(AmenitiesRef amenity) {
        if (amenitiesRepository.existsByName(amenity.getName())) {
            throw new RuntimeException("Tiện ích này đã tồn tại!");
        }
        return amenitiesRepository.save(amenity);
    }

    @Transactional
    public void deleteAmenity(Integer id) {
        amenitiesRepository.deleteById(id);
    }

    // --- 2. QUẢN LÝ GÓI CƯỚC (PACKAGES) ---

    public List<ServicePackage> getAllPackages() {
        return packageRepository.findAll();
    }

    @Transactional
    public ServicePackage createPackage(ServicePackage pkg) {
        return packageRepository.save(pkg);
    }

    @Transactional
    public ServicePackage updatePackage(Integer id, ServicePackage pkgDetails) {
        ServicePackage pkg = packageRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Gói cước không tồn tại"));

        pkg.setName(pkgDetails.getName());
        pkg.setPrice(pkgDetails.getPrice());
        pkg.setDurationDays(pkgDetails.getDurationDays());
        pkg.setPriorityLevel(pkgDetails.getPriorityLevel());

        return packageRepository.save(pkg);
    }

    @Transactional
    public void deletePackage(Integer id) {
        packageRepository.deleteById(id);
    }
}