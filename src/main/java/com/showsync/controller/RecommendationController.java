package com.showsync.controller;

import com.showsync.dto.recommendation.*;
import com.showsync.entity.recommendation.RecommendationType;
import com.showsync.security.UserPrincipal;
import com.showsync.service.RecommendationService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/recommendations")
@RequiredArgsConstructor
public class RecommendationController {

    private final RecommendationService recommendationService;

    @GetMapping("/personal")
    public ResponseEntity<Page<ContentRecommendationResponse>> getPersonal(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<ContentRecommendationResponse> result = recommendationService
                .getPersonalRecommendations(principal.getId(), pageable);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/realtime")
    public ResponseEntity<List<ContentRecommendationResponse>> getRealtime(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestParam(required = false) Long mediaId,
            @RequestParam(defaultValue = "10") int limit) {
        List<ContentRecommendationResponse> result = recommendationService
                .getRealTimeRecommendations(principal.getId(), mediaId, limit);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/trending")
    public ResponseEntity<List<ContentRecommendationResponse>> getTrending(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestParam(defaultValue = "10") int limit) {
        List<ContentRecommendationResponse> result = recommendationService
                .getTrendingRecommendations(principal.getId(), limit);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/groups")
    public ResponseEntity<Page<GroupRecommendationResponse>> getGroupRecs(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<GroupRecommendationResponse> result = recommendationService
                .getGroupRecommendations(principal.getId(), pageable);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/groups/{groupId}/content")
    public ResponseEntity<Page<ContentRecommendationResponse>> getGroupContent(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long groupId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<ContentRecommendationResponse> result = recommendationService
                .getGroupContentRecommendations(principal.getId(), groupId, pageable);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/view/{type}/{id}")
    public ResponseEntity<Void> markViewed(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable String type,
            @PathVariable Long id) {
        recommendationService.markRecommendationAsViewed(principal.getId(), type, id);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/dismiss/{type}/{id}")
    public ResponseEntity<Void> dismiss(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable String type,
            @PathVariable Long id,
            @RequestParam(required = false) String reason) {
        recommendationService.dismissRecommendation(principal.getId(), type, id, reason);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/feedback/{type}/{id}")
    public ResponseEntity<Void> feedback(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable String type,
            @PathVariable Long id,
            @RequestParam int rating,
            @RequestParam(required = false) String comment) {
        recommendationService.submitFeedback(principal.getId(), type, id, rating, comment);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/generate")
    public ResponseEntity<RecommendationGenerationSummary> generateForAll() {
        RecommendationGenerationSummary summary = recommendationService.generateRecommendationsForAllUsers();
        return ResponseEntity.ok(summary);
    }

    @PostMapping("/generate/me")
    public ResponseEntity<Integer> generateForUser(@AuthenticationPrincipal UserPrincipal principal) {
        int count = recommendationService.generateRecommendationsForUser(principal.getId());
        return ResponseEntity.ok(count);
    }

    @GetMapping("/analytics")
    public ResponseEntity<RecommendationAnalytics> analytics(@RequestParam(defaultValue = "30") int days) {
        return ResponseEntity.ok(recommendationService.getRecommendationAnalytics(days));
    }

    @GetMapping("/insights/me")
    public ResponseEntity<UserRecommendationInsights> myInsights(
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(recommendationService.getUserRecommendationInsights(principal.getId()));
    }

    @GetMapping("/summary/me")
    public ResponseEntity<RecommendationSummary> mySummary(
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(recommendationService.getRecommendationSummary(principal.getId()));
    }

    @GetMapping("/similar/{mediaId}")
    public ResponseEntity<List<ContentRecommendationResponse>> similar(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long mediaId,
            @RequestParam(defaultValue = "10") int limit) {
        return ResponseEntity.ok(recommendationService.findSimilarContent(principal.getId(), mediaId, limit));
    }

    @GetMapping("/by-type")
    public ResponseEntity<List<ContentRecommendationResponse>> byType(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestParam RecommendationType type,
            @RequestParam(defaultValue = "10") int limit) {
        return ResponseEntity.ok(recommendationService.getRecommendationsByType(principal.getId(), type, limit));
    }
}


