package com.showsync.repository;

import com.showsync.entity.UserMediaInteraction;
import com.showsync.entity.UserMediaInteraction.Status;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserMediaInteractionRepository extends JpaRepository<UserMediaInteraction, Long> {
    List<UserMediaInteraction> findByUserId(Long userId);
    List<UserMediaInteraction> findByUserIdAndStatus(Long userId, Status status);
    Optional<UserMediaInteraction> findByUserIdAndMediaId(Long userId, Long mediaId);
    List<UserMediaInteraction> findByMediaId(Long mediaId);
    List<UserMediaInteraction> findByUserIdAndIsFavoriteTrue(Long userId);
} 