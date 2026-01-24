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
// 🟢 GIỮ NGUYÊN ĐƯỜNG DẪN NÀY ĐỂ ĐẢM BẢO TÍNH CẤU TRÚC
@RequestMapping("/api/admin/master-data")
@RequiredArgsConstructor
public class AdminMasterDataController {

    private final AdminMasterDataServiceImpl masterDataService;

    // --- API TIỆN ÍCH ---

    @GetMapping("/amenities")
    // 🟢 Cho phép tất cả người dùng đã đăng nhập xem tiện ích
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<AmenitiesRef>> getAllAmenities() {
        return ResponseEntity.ok(masterDataService.getAllAmenities());
    }

    @PostMapping("/amenities")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<AmenitiesRef> createAmenity(@RequestBody AmenitiesRef amenity) {
        return ResponseEntity.ok(masterDataService.createAmenity(amenity));
    }

    // --- API GÓI CƯỚC ---

    @GetMapping("/packages")
    // 🟢 QUAN TRỌNG: Cho phép cả ADMIN và LANDLORD xem danh sách gói cước
    @PreAuthorize("hasAnyRole('ADMIN', 'LANDLORD')")
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
    public ResponseEntity<ServicePackage> updatePackage(@PathVariable Long id, @RequestBody ServicePackage pkg) {
        return ResponseEntity.ok(masterDataService.updatePackage(id, pkg));
    }

    @DeleteMapping("/packages/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> deletePackage(@PathVariable Long id) {
        masterDataService.deletePackage(id);
        return ResponseEntity.ok("Đã xóa gói cước");
    }
}