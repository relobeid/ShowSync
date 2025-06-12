package com.showsync.repository;

import com.showsync.entity.Media;
import com.showsync.entity.Media.MediaType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MediaRepository extends JpaRepository<Media, Long> {
    List<Media> findByType(MediaType type);
    Optional<Media> findByExternalIdAndExternalSource(String externalId, String externalSource);
    List<Media> findByTitleContainingIgnoreCase(String title);
    List<Media> findByTitleContainingIgnoreCaseAndType(String title, MediaType type);
} 