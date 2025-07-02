package com.showsync.service.impl;

import com.showsync.dto.groupmedia.GroupMediaListRequest;
import com.showsync.dto.groupmedia.GroupMediaStatsResponse;
import com.showsync.dto.groupmedia.GroupMediaVoteRequest;
import com.showsync.entity.*;
import com.showsync.repository.*;
import com.showsync.service.GroupMediaService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;

/**
 * Comprehensive test suite for GroupMediaServiceImpl.
 * Tests all group media activities functionality including lists, voting, and activity feeds.
 * 
 * @author ShowSync Development Team
 * @version 1.0
 * @since 2024-12-16
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
class GroupMediaServiceImplTest {

    @Autowired
    private GroupMediaService groupMediaService;

    @Autowired
    private GroupRepository groupRepository;

    @Autowired
    private GroupMembershipRepository membershipRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private MediaRepository mediaRepository;

    @Autowired
    private GroupMediaListRepository groupMediaListRepository;

    @Autowired
    private GroupMediaVoteRepository groupMediaVoteRepository;

    @Autowired
    private GroupActivityRepository groupActivityRepository;

    @Autowired
    private UserMediaInteractionRepository userMediaInteractionRepository;

    private User testUser1;
    private User testUser2;
    private User testAdmin;
    private Group testGroup;
    private Media testMovie;
    private Media testTvShow;

    @BeforeEach
    void setUp() {
        // Create test users
        testUser1 = createTestUser("testuser1", "test1@example.com", "Test User 1");
        testUser2 = createTestUser("testuser2", "test2@example.com", "Test User 2");
        testAdmin = createTestUser("testadmin", "admin@example.com", "Test Admin");

        // Create test group
        testGroup = createTestGroup("Test Group", testAdmin);

        // Create group memberships
        createMembership(testAdmin, testGroup, GroupMembership.MembershipRole.OWNER);
        createMembership(testUser1, testGroup, GroupMembership.MembershipRole.MEMBER);
        createMembership(testUser2, testGroup, GroupMembership.MembershipRole.MEMBER);

        // Create test media
        testMovie = createTestMedia("550", "tmdb", "Fight Club", Media.MediaType.MOVIE);
        testTvShow = createTestMedia("1399", "tmdb", "Game of Thrones", Media.MediaType.TV_SHOW);
    }

    // Group Media List Tests

    @Test
    @DisplayName("Should add media to group list successfully")
    void shouldAddMediaToGroupList() {
        // Given
        GroupMediaListRequest request = createValidMediaListRequest();

        // When
        GroupMediaList result = groupMediaService.addMediaToGroupList(testGroup.getId(), request, testUser1);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getGroup().getId()).isEqualTo(testGroup.getId());
        assertThat(result.getListType()).isEqualTo(GroupMediaList.ListType.CURRENTLY_WATCHING);
        assertThat(result.getAddedBy().getId()).isEqualTo(testUser1.getId());

        // Verify activity was recorded
        List<GroupActivity> activities = groupActivityRepository.findByGroupOrderByCreatedAtDesc(
                testGroup, PageRequest.of(0, 10)).getContent();
        assertThat(activities).hasSize(1);
        assertThat(activities.get(0).getActivityType()).isEqualTo(GroupActivity.ActivityType.MEDIA_ADDED_TO_LIST);
    }

    @Test
    @DisplayName("Should not allow duplicate media in group list")
    void shouldNotAllowDuplicateMediaInGroupList() {
        // Given
        GroupMediaListRequest request = createValidMediaListRequest();
        groupMediaService.addMediaToGroupList(testGroup.getId(), request, testUser1);

        // When & Then
        assertThatThrownBy(() -> groupMediaService.addMediaToGroupList(testGroup.getId(), request, testUser2))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Media is already in the group's list");
    }

    @Test
    @DisplayName("Should throw exception when non-member tries to add media")
    void shouldThrowExceptionWhenNonMemberTriesToAddMedia() {
        // Given
        User nonMember = createTestUser("nonmember", "nonmember@example.com", "Non Member");
        GroupMediaListRequest request = createValidMediaListRequest();

        // When & Then
        assertThatThrownBy(() -> groupMediaService.addMediaToGroupList(testGroup.getId(), request, nonMember))
                .isInstanceOf(SecurityException.class)
                .hasMessage("You must be an active member of this group");
    }

    @Test
    @DisplayName("Should get group media list with pagination")
    void shouldGetGroupMediaListWithPagination() {
        // Given
        GroupMediaListRequest request1 = createValidMediaListRequest();
        GroupMediaListRequest request2 = new GroupMediaListRequest();
        request2.setExternalId("1399");
        request2.setExternalSource("tmdb");
        request2.setListType(GroupMediaList.ListType.PLAN_TO_WATCH);

        groupMediaService.addMediaToGroupList(testGroup.getId(), request1, testUser1);
        groupMediaService.addMediaToGroupList(testGroup.getId(), request2, testUser2);

        // When
        Pageable pageable = PageRequest.of(0, 10);
        Page<GroupMediaList> result = groupMediaService.getGroupMediaList(testGroup.getId(), null, pageable, testUser1);

        // Then
        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getTotalElements()).isEqualTo(2);
    }

    @Test
    @DisplayName("Should cast media vote successfully")
    void shouldCastMediaVoteSuccessfully() {
        // Given
        GroupMediaVoteRequest request = new GroupMediaVoteRequest();
        request.setVoteType(GroupMediaVote.VoteType.WATCH_NEXT);

        // When
        GroupMediaVote vote = groupMediaService.castMediaVote(testGroup.getId(), testMovie.getId(), request, testUser1);

        // Then
        assertThat(vote).isNotNull();
        assertThat(vote.getVoteType()).isEqualTo(GroupMediaVote.VoteType.WATCH_NEXT);
        assertThat(vote.getUser().getId()).isEqualTo(testUser1.getId());
        assertThat(vote.getMedia().getId()).isEqualTo(testMovie.getId());

        // Verify activity was recorded
        List<GroupActivity> activities = groupActivityRepository.findByGroupOrderByCreatedAtDesc(
                testGroup, PageRequest.of(0, 10)).getContent();
        assertThat(activities).hasSize(1);
        assertThat(activities.get(0).getActivityType()).isEqualTo(GroupActivity.ActivityType.MEDIA_VOTE_CAST);
    }

    @Test
    @DisplayName("Should get group activity feed")
    void shouldGetGroupActivityFeed() {
        // Given
        GroupMediaListRequest request = createValidMediaListRequest();
        groupMediaService.addMediaToGroupList(testGroup.getId(), request, testUser1);

        GroupMediaVoteRequest voteRequest = new GroupMediaVoteRequest();
        voteRequest.setVoteType(GroupMediaVote.VoteType.WATCH_NEXT);
        groupMediaService.castMediaVote(testGroup.getId(), testMovie.getId(), voteRequest, testUser2);

        // When
        Pageable pageable = PageRequest.of(0, 10);
        Page<GroupActivity> activities = groupMediaService.getGroupActivityFeed(testGroup.getId(), pageable, testUser1);

        // Then
        assertThat(activities.getContent()).hasSize(2);
        assertThat(activities.getContent()).extracting(GroupActivity::getActivityType)
                .containsExactlyInAnyOrder(
                        GroupActivity.ActivityType.MEDIA_ADDED_TO_LIST,
                        GroupActivity.ActivityType.MEDIA_VOTE_CAST);
    }

    @Test
    @DisplayName("Should get group media statistics")
    void shouldGetGroupMediaStatistics() {
        // Given
        GroupMediaListRequest request1 = createValidMediaListRequest(); // CURRENTLY_WATCHING
        GroupMediaListRequest request2 = new GroupMediaListRequest();
        request2.setExternalId("1399");
        request2.setExternalSource("tmdb");
        request2.setListType(GroupMediaList.ListType.COMPLETED);

        groupMediaService.addMediaToGroupList(testGroup.getId(), request1, testUser1);
        groupMediaService.addMediaToGroupList(testGroup.getId(), request2, testUser2);

        // Add some votes
        GroupMediaVoteRequest voteRequest = new GroupMediaVoteRequest();
        voteRequest.setVoteType(GroupMediaVote.VoteType.WATCH_NEXT);
        groupMediaService.castMediaVote(testGroup.getId(), testMovie.getId(), voteRequest, testUser1);

        // When
        GroupMediaStatsResponse stats = groupMediaService.getGroupMediaStatistics(testGroup.getId(), testUser1);

        // Then
        assertThat(stats).isNotNull();
        assertThat(stats.getTotalMedia()).isEqualTo(2);
        assertThat(stats.getCurrentlyWatching()).isEqualTo(1);
        assertThat(stats.getCompleted()).isEqualTo(1);
        assertThat(stats.getTotalVotes()).isEqualTo(1);
        assertThat(stats.getUniqueMediaVotedOn()).isEqualTo(1);
        assertThat(stats.getActiveVoters()).isEqualTo(1);
    }

    // Helper Methods

    private User createTestUser(String username, String email, String displayName) {
        User user = new User();
        user.setUsername(username);
        user.setEmail(email);
        user.setDisplayName(displayName);
        user.setPassword("encodedPassword");
        user.setRole(User.Role.USER);
        user.setActive(true);
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());
        return userRepository.save(user);
    }

    private Group createTestGroup(String name, User createdBy) {
        Group group = new Group();
        group.setName(name);
        group.setDescription("A test group for integration testing");
        group.setPrivacySetting(Group.PrivacySetting.PUBLIC);
        group.setCreatedBy(createdBy);
        group.setMaxMembers(50);
        group.setActive(true);
        group.setCreatedAt(LocalDateTime.now());
        group.setUpdatedAt(LocalDateTime.now());
        return groupRepository.save(group);
    }

    private GroupMembership createMembership(User user, Group group, GroupMembership.MembershipRole role) {
        GroupMembership membership = new GroupMembership();
        membership.setUser(user);
        membership.setGroup(group);
        membership.setRole(role);
        membership.setStatus(GroupMembership.MembershipStatus.ACTIVE);
        membership.setJoinedAt(LocalDateTime.now());
        membership.setCreatedAt(LocalDateTime.now());
        membership.setUpdatedAt(LocalDateTime.now());
        return membershipRepository.save(membership);
    }

    private Media createTestMedia(String externalId, String externalSource, String title, Media.MediaType type) {
        Media media = new Media();
        media.setExternalId(externalId);
        media.setExternalSource(externalSource);
        media.setTitle(title);
        media.setType(type);
        media.setDescription("Test description for " + title);
        media.setCreatedAt(LocalDateTime.now());
        media.setUpdatedAt(LocalDateTime.now());
        return mediaRepository.save(media);
    }

    private GroupMediaListRequest createValidMediaListRequest() {
        GroupMediaListRequest request = new GroupMediaListRequest();
        request.setExternalId("550");
        request.setExternalSource("tmdb");
        request.setListType(GroupMediaList.ListType.CURRENTLY_WATCHING);
        return request;
    }
} 