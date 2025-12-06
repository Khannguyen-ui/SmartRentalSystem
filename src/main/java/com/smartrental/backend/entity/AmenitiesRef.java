package com.smartrental.backend.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "amenities_ref")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AmenitiesRef {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(unique = true, nullable = false)
    private String name; // VD: WIFI, AC, PARKING

    @Column(name = "icon_url")
    private String iconUrl; // Link icon
}