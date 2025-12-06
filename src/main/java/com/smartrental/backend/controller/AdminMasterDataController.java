package com.smartrental.backend.controller;

import com.smartrental.backend.entity.AmenitiesRef;
import com.smartrental.backend.entity.ServicePackage;
import com.smartrental.backend.service.impl.AdminMasterDataServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
// Chỉ cho phép ADMIN truy cập
@PreAuthorize("hasRole('ADMIN')")
public class AdminMasterDataController {

    private final AdminMasterDataServiceImpl masterDataService;

    // --- API TIỆN ÍCH ---

    // GET /api/admin/amenities
    @GetMapping("/amenities")
    public ResponseEntity<List<AmenitiesRef>> getAllAmenities() {
        return ResponseEntity.ok(masterDataService.getAllAmenities());
    }

    // POST /api/admin/amenities
    @PostMapping("/amenities")
    public ResponseEntity<AmenitiesRef> createAmenity(@RequestBody AmenitiesRef amenity) {
        return ResponseEntity.ok(masterDataService.createAmenity(amenity));
    }

    // DELETE /api/admin/amenities/{id}
    @DeleteMapping("/amenities/{id}")
    public ResponseEntity<?> deleteAmenity(@PathVariable Integer id) {
        masterDataService.deleteAmenity(id);
        return ResponseEntity.ok("Đã xóa tiện ích");
    }

    // --- API GÓI CƯỚC ---

    // GET /api/admin/packages
    @GetMapping("/packages")
    public ResponseEntity<List<ServicePackage>> getAllPackages() {
        return ResponseEntity.ok(masterDataService.getAllPackages());
    }

    // POST /api/admin/packages
    @PostMapping("/packages")
    public ResponseEntity<ServicePackage> createPackage(@RequestBody ServicePackage pkg) {
        return ResponseEntity.ok(masterDataService.createPackage(pkg));
    }

    // PUT /api/admin/packages/{id}
    @PutMapping("/packages/{id}")
    public ResponseEntity<ServicePackage> updatePackage(@PathVariable Integer id, @RequestBody ServicePackage pkg) {
        return ResponseEntity.ok(masterDataService.updatePackage(id, pkg));
    }

    // DELETE /api/admin/packages/{id}
    @DeleteMapping("/packages/{id}")
    public ResponseEntity<?> deletePackage(@PathVariable Integer id) {
        masterDataService.deletePackage(id);
        return ResponseEntity.ok("Đã xóa gói cước");
    }
}