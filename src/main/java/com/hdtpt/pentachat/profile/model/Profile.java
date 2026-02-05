package com.hdtpt.pentachat.profile.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import jakarta.persistence.Column;
import java.time.LocalDateTime;

@Entity
@Table(name = "profiles")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Profile {

    @Id
    private String id;

    @Column(nullable = false, unique = true)
    private String userId;

    @Column(length = 255)
    private String fullName;

    @Column(length = 500)
    private String bio;

    @Column(length = 255)
    private String avatar;

    @Column(length = 255)
    private String phoneNumber;

    @Column(length = 255)
    private String address;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;
}
