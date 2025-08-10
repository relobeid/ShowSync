package com.showsync.controller;

import com.showsync.dto.recommendation.ContentRecommendationResponse;
import com.showsync.entity.recommendation.RecommendationType;
import com.showsync.security.UserPrincipal;
import com.showsync.service.RecommendationService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@DisplayName("RecommendationController unit tests")
class RecommendationControllerTest {

    @Test
    @DisplayName("personal endpoint delegates to service and returns body")
    void personalDelegates() {
        RecommendationService service = mock(RecommendationService.class);
        RecommendationController controller = new RecommendationController(service);

        UserPrincipal principal = mock(UserPrincipal.class);
        when(principal.getId()).thenReturn(1L);

        Page<ContentRecommendationResponse> page = new PageImpl<>(List.of(), PageRequest.of(0, 10), 0);
        when(service.getPersonalRecommendations(eq(1L), any(Pageable.class))).thenReturn(page);

        ResponseEntity<Page<ContentRecommendationResponse>> resp = controller.getPersonal(principal, 0, 10);
        assertEquals(200, resp.getStatusCode().value());
        assertEquals(0, resp.getBody().getTotalElements());
        verify(service, times(1)).getPersonalRecommendations(eq(1L), any(Pageable.class));
    }

    @Test
    @DisplayName("trending endpoint delegates to service and returns list")
    void trendingDelegates() {
        RecommendationService service = mock(RecommendationService.class);
        RecommendationController controller = new RecommendationController(service);
        UserPrincipal principal = mock(UserPrincipal.class);
        when(principal.getId()).thenReturn(1L);

        when(service.getTrendingRecommendations(1L, 5)).thenReturn(List.of());

        ResponseEntity<List<ContentRecommendationResponse>> resp = controller.getTrending(principal, 5);
        assertEquals(200, resp.getStatusCode().value());
        assertNotNull(resp.getBody());
        verify(service).getTrendingRecommendations(1L, 5);
    }

    @Test
    @DisplayName("by-type endpoint passes enum and limit")
    void byTypeDelegates() {
        RecommendationService service = mock(RecommendationService.class);
        RecommendationController controller = new RecommendationController(service);
        UserPrincipal principal = mock(UserPrincipal.class);
        when(principal.getId()).thenReturn(42L);

        when(service.getRecommendationsByType(42L, RecommendationType.TRENDING, 3)).thenReturn(List.of());
        ResponseEntity<List<ContentRecommendationResponse>> resp = controller.byType(principal, RecommendationType.TRENDING, 3);
        assertEquals(200, resp.getStatusCode().value());
        verify(service).getRecommendationsByType(42L, RecommendationType.TRENDING, 3);
    }
}


