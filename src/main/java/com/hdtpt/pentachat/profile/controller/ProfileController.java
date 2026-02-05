package com.hdtpt.pentachat.profile.controller;

import org.springframework.web.bind.annotation.*;
import com.hdtpt.pentachat.profile.service.ProfileService;
import com.hdtpt.pentachat.profile.dto.request.UpdateProfileRequest;
import com.hdtpt.pentachat.profile.dto.response.ProfileResponse;
import com.hdtpt.pentachat.dto.response.ApiResponse;

@RestController
@RequestMapping("/api/profiles")
public class ProfileController {
    private final ProfileService profileService;

    public ProfileController(ProfileService profileService) {
        this.profileService = profileService;
    }

    @GetMapping("/{userId}")
    public ApiResponse getProfile(@PathVariable String userId) {
        ProfileResponse profile = profileService.getProfile(userId);
        return ApiResponse.builder()
                .success(true)
                .message("OK")
                .data(profile)
                .build();
    }

    @PutMapping("/{userId}")
    public ApiResponse updateProfile(
            @PathVariable String userId,
            @RequestBody UpdateProfileRequest request) {
        ProfileResponse updatedProfile = profileService.updateProfile(userId, request);
        return ApiResponse.builder()
                .success(true)
                .message("Profile updated successfully")
                .data(updatedProfile)
                .build();
    }
}
