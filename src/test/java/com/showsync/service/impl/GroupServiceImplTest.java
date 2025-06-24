package com.showsync.service.impl;

import com.showsync.config.TestConfig;
import com.showsync.dto.group.CreateGroupRequest;
import com.showsync.dto.group.UpdateGroupRequest;
import com.showsync.entity.Group;
import com.showsync.entity.GroupMembership;
import com.showsync.entity.User;
import com.showsync.repository.GroupMembershipRepository;
import com.showsync.repository.GroupRepository;
import com.showsync.repository.UserRepository;
import com.showsync.service.GroupService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.*;

/**
 * Comprehensive tests for GroupServiceImpl.
 * Tests all CRUD operations, membership management, and security validations.
 * 
 * @author ShowSync Development Team
 * @version 1.0
 * @since 2024-12-16
 */
@DataJpaTest
@Import({TestConfig.class, GroupServiceImpl.class})
@ActiveProfiles("test")
class GroupServiceImplTest {

    @Autowired
    private GroupService groupService;

    @Autowired
    private GroupRepository groupRepository;

    @Autowired
    private GroupMembershipRepository membershipRepository;

    @Autowired
    private UserRepository userRepository;

    private User testUser1;
    private User testUser2;
    private User testUser3;

    @BeforeEach
    void setUp() {
        // Clean up any existing data
        membershipRepository.deleteAll();
        groupRepository.deleteAll();
        userRepository.deleteAll();

        // Create test users
        testUser1 = createTestUser("testuser1", "test1@example.com", "Test User 1");
        testUser2 = createTestUser("testuser2", "test2@example.com", "Test User 2");
        testUser3 = createTestUser("testuser3", "test3@example.com", "Test User 3");
    }

    private User createTestUser(String username, String email, String displayName) {
        User user = new User();
        user.setUsername(username);
        user.setEmail(email);
        user.setDisplayName(displayName);
        user.setPassword("hashedPassword");
        user.setActive(true);
        user.setEmailVerified(true);
        user.setRole(User.Role.USER);
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());
        return userRepository.save(user);
    }

    private CreateGroupRequest createValidGroupRequest() {
        CreateGroupRequest request = new CreateGroupRequest();
        request.setName("Test Group");
        request.setDescription("A test group for testing purposes");
        request.setPrivacySetting(Group.PrivacySetting.PUBLIC);
        request.setMaxMembers(50);
        return request;
    }

    @Nested
    @DisplayName("Group Creation Tests")
    class GroupCreationTests {

        @Test
        @DisplayName("Should create group successfully with valid request")
        void shouldCreateGroupSuccessfully() {
            // Given
            CreateGroupRequest request = createValidGroupRequest();

            // When
            Group createdGroup = groupService.createGroup(request, testUser1);

            // Then
            assertThat(createdGroup).isNotNull();
            assertThat(createdGroup.getId()).isNotNull();
            assertThat(createdGroup.getName()).isEqualTo("Test Group");
            assertThat(createdGroup.getDescription()).isEqualTo("A test group for testing purposes");
            assertThat(createdGroup.getPrivacySetting()).isEqualTo(Group.PrivacySetting.PUBLIC);
            assertThat(createdGroup.getCreatedBy()).isEqualTo(testUser1);
            assertThat(createdGroup.getMaxMembers()).isEqualTo(50);
            assertThat(createdGroup.isActive()).isTrue();

            // Verify owner membership was created
            var memberships = membershipRepository.findByUserAndGroup(testUser1, createdGroup);
            assertThat(memberships).isPresent();
            assertThat(memberships.get().getRole()).isEqualTo(GroupMembership.MembershipRole.OWNER);
            assertThat(memberships.get().getStatus()).isEqualTo(GroupMembership.MembershipStatus.ACTIVE);
        }

        @Test
        @DisplayName("Should reject group creation with null request")
        void shouldRejectNullRequest() {
            // When & Then
            assertThatThrownBy(() -> groupService.createGroup(null, testUser1))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Create group request cannot be null");
        }

        @Test
        @DisplayName("Should reject group creation with null user")
        void shouldRejectNullUser() {
            // Given
            CreateGroupRequest request = createValidGroupRequest();

            // When & Then
            assertThatThrownBy(() -> groupService.createGroup(request, null))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Valid user is required");
        }

        @Test
        @DisplayName("Should reject group creation with invalid name")
        void shouldRejectInvalidName() {
            // Given
            CreateGroupRequest request = createValidGroupRequest();
            request.setName("ab"); // Too short

            // When & Then
            assertThatThrownBy(() -> groupService.createGroup(request, testUser1))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Group name must be between 3 and 100 characters");
        }

        @Test
        @DisplayName("Should reject group creation with duplicate name")
        void shouldRejectDuplicateName() {
            // Given
            CreateGroupRequest request1 = createValidGroupRequest();
            CreateGroupRequest request2 = createValidGroupRequest();
            request2.setName("Test Group"); // Same name

            // When
            groupService.createGroup(request1, testUser1);

            // Then
            assertThatThrownBy(() -> groupService.createGroup(request2, testUser2))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("A group with this name already exists");
        }

        @Test
        @DisplayName("Should handle null description gracefully")
        void shouldHandleNullDescription() {
            // Given
            CreateGroupRequest request = createValidGroupRequest();
            request.setDescription(null);

            // When
            Group createdGroup = groupService.createGroup(request, testUser1);

            // Then
            assertThat(createdGroup.getDescription()).isNull();
        }

        @Test
        @DisplayName("Should trim whitespace from name and description")
        void shouldTrimWhitespace() {
            // Given
            CreateGroupRequest request = createValidGroupRequest();
            request.setName("  Test Group  ");
            request.setDescription("  Description with spaces  ");

            // When
            Group createdGroup = groupService.createGroup(request, testUser1);

            // Then
            assertThat(createdGroup.getName()).isEqualTo("Test Group");
            assertThat(createdGroup.getDescription()).isEqualTo("Description with spaces");
        }
    }

    @Nested
    @DisplayName("Group Update Tests")
    class GroupUpdateTests {

        private Group testGroup;

        @BeforeEach
        void setUpGroup() {
            CreateGroupRequest request = createValidGroupRequest();
            testGroup = groupService.createGroup(request, testUser1);
        }

        @Test
        @DisplayName("Should update group successfully by owner")
        void shouldUpdateGroupSuccessfully() {
            // Given
            UpdateGroupRequest request = new UpdateGroupRequest();
            request.setName("Updated Group Name");
            request.setDescription("Updated description");
            request.setPrivacySetting(Group.PrivacySetting.PRIVATE);
            request.setMaxMembers(100);

            // When
            Group updatedGroup = groupService.updateGroup(testGroup.getId(), request, testUser1);

            // Then
            assertThat(updatedGroup.getName()).isEqualTo("Updated Group Name");
            assertThat(updatedGroup.getDescription()).isEqualTo("Updated description");
            assertThat(updatedGroup.getPrivacySetting()).isEqualTo(Group.PrivacySetting.PRIVATE);
            assertThat(updatedGroup.getMaxMembers()).isEqualTo(100);
        }

        @Test
        @DisplayName("Should reject update by non-admin user")
        void shouldRejectUpdateByNonAdmin() {
            // Given
            UpdateGroupRequest request = new UpdateGroupRequest();
            request.setName("Updated Group Name");

            // When & Then
            assertThatThrownBy(() -> groupService.updateGroup(testGroup.getId(), request, testUser2))
                    .isInstanceOf(SecurityException.class)
                    .hasMessage("You do not have permission to update this group");
        }

        @Test
        @DisplayName("Should reject update to duplicate group name")
        void shouldRejectDuplicateNameUpdate() {
            // Given
            CreateGroupRequest anotherGroupRequest = createValidGroupRequest();
            anotherGroupRequest.setName("Another Group");
            groupService.createGroup(anotherGroupRequest, testUser2);

            UpdateGroupRequest request = new UpdateGroupRequest();
            request.setName("Another Group");

            // When & Then
            assertThatThrownBy(() -> groupService.updateGroup(testGroup.getId(), request, testUser1))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("A group with this name already exists");
        }

        @Test
        @DisplayName("Should allow partial updates")
        void shouldAllowPartialUpdates() {
            // Given
            UpdateGroupRequest request = new UpdateGroupRequest();
            request.setName("Only Name Update");
            // Other fields left null

            // When
            Group updatedGroup = groupService.updateGroup(testGroup.getId(), request, testUser1);

            // Then
            assertThat(updatedGroup.getName()).isEqualTo("Only Name Update");
            assertThat(updatedGroup.getDescription()).isEqualTo(testGroup.getDescription()); // Unchanged
            assertThat(updatedGroup.getPrivacySetting()).isEqualTo(testGroup.getPrivacySetting()); // Unchanged
        }
    }

    @Nested
    @DisplayName("Group Membership Tests")
    class GroupMembershipTests {

        private Group publicGroup;
        private Group privateGroup;

        @BeforeEach
        void setUpGroups() {
            CreateGroupRequest publicRequest = createValidGroupRequest();
            publicRequest.setName("Public Group");
            publicRequest.setPrivacySetting(Group.PrivacySetting.PUBLIC);
            publicGroup = groupService.createGroup(publicRequest, testUser1);

            CreateGroupRequest privateRequest = createValidGroupRequest();
            privateRequest.setName("Private Group");
            privateRequest.setPrivacySetting(Group.PrivacySetting.PRIVATE);
            privateGroup = groupService.createGroup(privateRequest, testUser1);
        }

        @Test
        @DisplayName("Should allow user to join public group immediately")
        void shouldJoinPublicGroupImmediately() {
            // When
            GroupMembership membership = groupService.joinGroup(publicGroup.getId(), testUser2);

            // Then
            assertThat(membership.getUser()).isEqualTo(testUser2);
            assertThat(membership.getGroup()).isEqualTo(publicGroup);
            assertThat(membership.getRole()).isEqualTo(GroupMembership.MembershipRole.MEMBER);
            assertThat(membership.getStatus()).isEqualTo(GroupMembership.MembershipStatus.ACTIVE);
        }

        @Test
        @DisplayName("Should create pending membership for private group")
        void shouldCreatePendingMembershipForPrivateGroup() {
            // When
            GroupMembership membership = groupService.joinGroup(privateGroup.getId(), testUser2);

            // Then
            assertThat(membership.getUser()).isEqualTo(testUser2);
            assertThat(membership.getGroup()).isEqualTo(privateGroup);
            assertThat(membership.getRole()).isEqualTo(GroupMembership.MembershipRole.MEMBER);
            assertThat(membership.getStatus()).isEqualTo(GroupMembership.MembershipStatus.PENDING);
        }

        @Test
        @DisplayName("Should reject duplicate join requests")
        void shouldRejectDuplicateJoinRequests() {
            // Given
            groupService.joinGroup(publicGroup.getId(), testUser2);

            // When & Then
            assertThatThrownBy(() -> groupService.joinGroup(publicGroup.getId(), testUser2))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("You are already a member of this group");
        }

        @Test
        @DisplayName("Should allow user to leave group")
        void shouldAllowUserToLeaveGroup() {
            // Given
            groupService.joinGroup(publicGroup.getId(), testUser2);

            // When
            groupService.leaveGroup(publicGroup.getId(), testUser2, testUser2);

            // Then
            var membership = membershipRepository.findByUserAndGroup(testUser2, publicGroup);
            assertThat(membership).isEmpty();
        }

        @Test
        @DisplayName("Should prevent owner from leaving group")
        void shouldPreventOwnerFromLeavingGroup() {
            // When & Then
            assertThatThrownBy(() -> groupService.leaveGroup(publicGroup.getId(), testUser1, testUser1))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Group owner cannot leave. Transfer ownership first or delete the group");
        }
    }

    @Nested
    @DisplayName("Group Search and Retrieval Tests")
    class GroupSearchTests {

        @BeforeEach
        void setUpGroups() {
            CreateGroupRequest request1 = createValidGroupRequest();
            request1.setName("Movie Enthusiasts");
            request1.setPrivacySetting(Group.PrivacySetting.PUBLIC);
            groupService.createGroup(request1, testUser1);

            CreateGroupRequest request2 = createValidGroupRequest();
            request2.setName("TV Show Fans");
            request2.setPrivacySetting(Group.PrivacySetting.PUBLIC);
            groupService.createGroup(request2, testUser2);

            CreateGroupRequest request3 = createValidGroupRequest();
            request3.setName("Book Club");
            request3.setPrivacySetting(Group.PrivacySetting.PRIVATE);
            groupService.createGroup(request3, testUser3);
        }

        @Test
        @DisplayName("Should search groups by name")
        void shouldSearchGroupsByName() {
            // When
            Page<Group> results = groupService.searchGroups("Movie", PageRequest.of(0, 10), testUser1);

            // Then
            assertThat(results.getContent()).hasSize(1);
            assertThat(results.getContent().get(0).getName()).isEqualTo("Movie Enthusiasts");
        }

        @Test
        @DisplayName("Should get public groups")
        void shouldGetPublicGroups() {
            // When
            Page<Group> results = groupService.getPublicGroups(PageRequest.of(0, 10), testUser1);

            // Then
            assertThat(results.getContent()).hasSize(2);
            assertThat(results.getContent())
                    .extracting(Group::getName)
                    .containsExactlyInAnyOrder("Movie Enthusiasts", "TV Show Fans");
        }

        @Test
        @DisplayName("Should check group name availability")
        void shouldCheckGroupNameAvailability() {
            // When & Then
            assertThat(groupService.isGroupNameAvailable("Unique Group Name")).isTrue();
            assertThat(groupService.isGroupNameAvailable("Movie Enthusiasts")).isFalse();
            assertThat(groupService.isGroupNameAvailable("")).isFalse();
            assertThat(groupService.isGroupNameAvailable(null)).isFalse();
        }
    }

    @Nested
    @DisplayName("Group Administration Tests")
    class GroupAdministrationTests {

        private Group testGroup;
        private GroupMembership memberMembership;

        @BeforeEach
        void setUpGroupWithMembers() {
            CreateGroupRequest request = createValidGroupRequest();
            testGroup = groupService.createGroup(request, testUser1);
            
            // Add a regular member
            GroupMembership membership = groupService.joinGroup(testGroup.getId(), testUser2);
            memberMembership = membership;
        }

        @Test
        @DisplayName("Should promote member to admin")
        void shouldPromoteMemberToAdmin() {
            // When
            GroupMembership promoted = groupService.promoteMemberToAdmin(memberMembership.getId(), testUser1);

            // Then
            assertThat(promoted.getRole()).isEqualTo(GroupMembership.MembershipRole.ADMIN);
        }

        @Test
        @DisplayName("Should reject promotion by non-owner")
        void shouldRejectPromotionByNonOwner() {
            // When & Then
            assertThatThrownBy(() -> groupService.promoteMemberToAdmin(memberMembership.getId(), testUser3))
                    .isInstanceOf(SecurityException.class)
                    .hasMessage("You do not have owner permission for this group");
        }

        @Test
        @DisplayName("Should delete group successfully")
        void shouldDeleteGroupSuccessfully() {
            // When
            groupService.deleteGroup(testGroup.getId(), testUser1);

            // Then
            Group deletedGroup = groupRepository.findById(testGroup.getId()).orElse(null);
            assertThat(deletedGroup).isNotNull();
            assertThat(deletedGroup.isActive()).isFalse();
        }

        @Test
        @DisplayName("Should reject group deletion by non-owner")
        void shouldRejectGroupDeletionByNonOwner() {
            // When & Then
            assertThatThrownBy(() -> groupService.deleteGroup(testGroup.getId(), testUser2))
                    .isInstanceOf(SecurityException.class)
                    .hasMessage("You do not have owner permission for this group");
        }
    }

    @Nested
    @DisplayName("Capacity Management Tests")
    class CapacityManagementTests {

        @Test
        @DisplayName("Should reject joining group at capacity")
        void shouldRejectJoiningGroupAtCapacity() {
            // Given - Create a group with max 1 member (owner)
            CreateGroupRequest request = createValidGroupRequest();
            request.setMaxMembers(1);
            Group group = groupService.createGroup(request, testUser1);

            // When & Then - Should reject additional member
            assertThatThrownBy(() -> groupService.joinGroup(group.getId(), testUser2))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("This group has reached its maximum member capacity");
        }

        @Test
        @DisplayName("Should prevent setting max members below current count")
        void shouldPreventSettingMaxMembersBelowCurrentCount() {
            // Given
            Group group = groupService.createGroup(createValidGroupRequest(), testUser1);
            groupService.joinGroup(group.getId(), testUser2); // 2 members total

            UpdateGroupRequest request = new UpdateGroupRequest();
            request.setMaxMembers(1); // Below current count

            // When & Then
            assertThatThrownBy(() -> groupService.updateGroup(group.getId(), request, testUser1))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Cannot set max members to 1 when group already has");
        }
    }
} 