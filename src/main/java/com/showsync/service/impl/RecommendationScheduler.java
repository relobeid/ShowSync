package com.showsync.service.impl;

import com.showsync.config.RecommendationConfig;
import com.showsync.service.RecommendationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class RecommendationScheduler {

    private final RecommendationConfig config;
    private final RecommendationService recommendationService;

    @Scheduled(cron = "${showsync.recommendations.daily-generation-cron:0 15 3 * * *}")
    public void generateForAllUsersDaily() {
        if (!config.isEnableSchedulers()) return;
        log.info("[Scheduler] Starting daily recommendation generation for all users");
        recommendationService.generateRecommendationsForAllUsers();
    }

    @Scheduled(cron = "${showsync.recommendations.active-users-refresh-cron:0 10 * * * *}")
    public void refreshForActiveUsers() {
        if (!config.isEnableSchedulers()) return;
        int hoursBack = config.getActiveUsersHoursBack();
        log.info("[Scheduler] Refreshing recommendations for users active in last {} hours", hoursBack);
        recommendationService.refreshRecommendationsForActiveUsers(hoursBack);
    }
}


