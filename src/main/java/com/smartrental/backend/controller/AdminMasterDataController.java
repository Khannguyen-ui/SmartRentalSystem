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
// 1. XÓA DÒNG @PreAuthorize("hasRole('ADMIN')") Ở ĐÂY ĐI
public class AdminMasterDataController {

    private final AdminMasterDataServiceImpl masterDataService;

    // --- API TIỆN ÍCH ---

    // 2. Cho phép User/Landlord xem danh sách tiện ích
    @GetMapping("/amenities")
    @PreAuthorize("isAuthenticated()") // Ai đăng nhập rồi cũng xem được
    public ResponseEntity<List<AmenitiesRef>> getAllAmenities() {
        return ResponseEntity.ok(masterDataService.getAllAmenities());
    }

    @PostMapping("/amenities")
    @PreAuthorize("hasRole('ADMIN')") // Chỉ Admin được thêm
    public ResponseEntity<AmenitiesRef> createAmenity(@RequestBody AmenitiesRef amenity) {
        return ResponseEntity.ok(masterDataService.createAmenity(amenity));
    }

    @DeleteMapping("/amenities/{id}")
    @PreAuthorize("hasRole('ADMIN')") // Chỉ Admin được xóa
    public ResponseEntity<?> deleteAmenity(@PathVariable Integer id) {
        masterDataService.deleteAmenity(id);
        return ResponseEntity.ok("Đã xóa tiện ích");
    }

    // --- API GÓI CƯỚC ---

    // 3. Cho phép Chủ trọ xem gói cước để mua
    @GetMapping("/packages")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<ServicePackage>> getAllPackages() {
        return ResponseEntity.ok(masterDataService.getAllPackages());
    }

    @PostMapping("/packages")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ServicePackage> createPackage(@RequestBody ServicePackage pkg) {
        return ResponseEntity.ok(masterDataService.createPackage(pkg));
    }

    @PutMapping("/packages/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ServicePackage> updatePackage(@PathVariable Integer id, @RequestBody ServicePackage pkg) {
        return ResponseEntity.ok(masterDataService.updatePackage(id, pkg));
    }

    @DeleteMapping("/packages/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> deletePackage(@PathVariable Integer id) {
        masterDataService.deletePackage(id);
        return ResponseEntity.ok("Đã xóa gói cước");
    }
}