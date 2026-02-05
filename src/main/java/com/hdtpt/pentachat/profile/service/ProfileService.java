package com.hdtpt.pentachat.profile.service;

import org.springframework.stereotype.Service;
import com.hdtpt.pentachat.profile.model.Profile;
import com.hdtpt.pentachat.profile.repository.ProfileRepository;
import com.hdtpt.pentachat.profile.dto.request.UpdateProfileRequest;
import com.hdtpt.pentachat.profile.dto.response.ProfileResponse;
import com.hdtpt.pentachat.exception.AppException;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class ProfileService {
    private final ProfileRepository profileRepository;

    public ProfileService(ProfileRepository profileRepository) {
        this.profileRepository = profileRepository;
    }

    public ProfileResponse getProfile(String userId) {
        Profile profile = profileRepository.findByUserId(userId)
                .orElseThrow(() -> new AppException("Profile not found"));

        return mapToResponse(profile);
    }

    public ProfileResponse updateProfile(String userId, UpdateProfileRequest request) {
        Profile profile = profileRepository.findByUserId(userId)
                .orElseThrow(() -> new AppException("Profile not found"));

        if (request.getFullName() != null) {
            profile.setFullName(request.getFullName());
        }
        if (request.getBio() != null) {
            profile.setBio(request.getBio());
        }
        if (request.getAvatar() != null) {
            profile.setAvatar(request.getAvatar());
        }
        if (request.getPhoneNumber() != null) {
            profile.setPhoneNumber(request.getPhoneNumber());
        }
        if (request.getAddress() != null) {
            profile.setAddress(request.getAddress());
        }

        profile.setUpdatedAt(LocalDateTime.now());
        Profile updatedProfile = profileRepository.save(profile);

        return mapToResponse(updatedProfile);
    }

    public ProfileResponse createProfile(String userId) {
        Profile profile = Profile.builder()
                .id(UUID.randomUUID().toString())
                .userId(userId)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        Profile savedProfile = profileRepository.save(profile);
        return mapToResponse(savedProfile);
    }

    private ProfileResponse mapToResponse(Profile profile) {
        return ProfileResponse.builder()
                .id(profile.getId())
                .userId(profile.getUserId())
                .fullName(profile.getFullName())
                .bio(profile.getBio())
                .avatar(profile.getAvatar())
                .phoneNumber(profile.getPhoneNumber())
                .address(profile.getAddress())
                .createdAt(profile.getCreatedAt())
                .updatedAt(profile.getUpdatedAt())
                .build();
    }
}
