package com.hdtpt.pentachat.profile.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.hdtpt.pentachat.profile.model.Profile;
import java.util.Optional;

public interface ProfileRepository extends JpaRepository<Profile, String> {
    Optional<Profile> findByUserId(String userId);
}
