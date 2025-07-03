package com.showsync.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.showsync.config.TestConfigurationBase;
import com.showsync.dto.group.CreateGroupRequest;
import com.showsync.dto.group.UpdateGroupRequest;
import com.showsync.entity.Group;
import com.showsync.entity.GroupMembership;
import com.showsync.entity.User;
import com.showsync.repository.GroupMembershipRepository;
import com.showsync.repository.GroupRepository;
import com.showsync.repository.UserRepository;
import com.showsync.security.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for GroupController.
 * Tests all REST API endpoints with proper authentication and authorization.
 * 
 * @author ShowSync Development Team
 * @version 1.0
 * @since 2024-12-16
 */
@SpringBootTest
@AutoConfigureMockMvc
@Import(TestConfigurationBase.class)
@ActiveProfiles("test")
@TestPropertySource(locations = "classpath:test-common.properties")
@Transactional
class GroupControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private GroupRepository groupRepository;

    @Autowired
    private GroupMembershipRepository membershipRepository;

    @Autowired
    private JwtUtil jwtUtil;

    private User testUser1;
    private User testUser2;
    private String testUser1Token;
    private String testUser2Token;

    @BeforeEach
    void setUp() {
        // Clean up any existing data
        membershipRepository.deleteAll();
        groupRepository.deleteAll();
        userRepository.deleteAll();

        // Create test users
        testUser1 = createTestUser("testuser1", "test1@example.com", "Test User 1");
        testUser2 = createTestUser("testuser2", "test2@example.com", "Test User 2");
        
        // Generate JWT tokens
        testUser1Token = jwtUtil.generateToken(testUser1.getUsername(), testUser1.getRole().toString(), testUser1.getId());
        testUser2Token = jwtUtil.generateToken(testUser2.getUsername(), testUser2.getRole().toString(), testUser2.getId());
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
    @DisplayName("Group Creation API Tests")
    class GroupCreationApiTests {

        @Test
        @DisplayName("Should create group successfully with valid request")
        void shouldCreateGroupSuccessfully() throws Exception {
            CreateGroupRequest request = createValidGroupRequest();

            mockMvc.perform(post("/api/groups")
                    .header("Authorization", "Bearer " + testUser1Token)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.name").value("Test Group"))
                    .andExpect(jsonPath("$.description").value("A test group for testing purposes"))
                    .andExpect(jsonPath("$.privacySetting").value("PUBLIC"))
                    .andExpect(jsonPath("$.maxMembers").value(50))
                    .andExpect(jsonPath("$.active").value(true))
                    .andExpect(jsonPath("$.createdByUsername").value("testuser1"));
        }

        @Test
        @DisplayName("Should reject group creation without authentication")
        void shouldRejectUnauthenticatedRequest() throws Exception {
            CreateGroupRequest request = createValidGroupRequest();

            mockMvc.perform(post("/api/groups")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("Should reject invalid group request")
        void shouldRejectInvalidRequest() throws Exception {
            CreateGroupRequest request = new CreateGroupRequest();
            request.setName("ab"); // Too short
            request.setPrivacySetting(Group.PrivacySetting.PUBLIC);

            mockMvc.perform(post("/api/groups")
                    .header("Authorization", "Bearer " + testUser1Token)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("Group Retrieval API Tests")
    class GroupRetrievalApiTests {

        private Group testGroup;

        @BeforeEach
        void setUpGroup() {
            testGroup = createTestGroup();
        }

        private Group createTestGroup() {
            Group group = new Group();
            group.setName("Test Group");
            group.setDescription("Test Description");
            group.setPrivacySetting(Group.PrivacySetting.PUBLIC);
            group.setCreatedBy(testUser1);
            group.setMaxMembers(50);
            group.setActive(true);
            group.setCreatedAt(LocalDateTime.now());
            group.setUpdatedAt(LocalDateTime.now());
            
            Group savedGroup = groupRepository.save(group);
            
            // Create owner membership
            GroupMembership ownerMembership = new GroupMembership();
            ownerMembership.setGroup(savedGroup);
            ownerMembership.setUser(testUser1);
            ownerMembership.setRole(GroupMembership.MembershipRole.OWNER);
            ownerMembership.setStatus(GroupMembership.MembershipStatus.ACTIVE);
            ownerMembership.setJoinedAt(LocalDateTime.now());
            ownerMembership.setCreatedAt(LocalDateTime.now());
            ownerMembership.setUpdatedAt(LocalDateTime.now());
            membershipRepository.save(ownerMembership);
            
            return savedGroup;
        }

        @Test
        @DisplayName("Should get group by ID successfully")
        void shouldGetGroupById() throws Exception {
            mockMvc.perform(get("/api/groups/{id}", testGroup.getId())
                    .header("Authorization", "Bearer " + testUser1Token))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(testGroup.getId()))
                    .andExpect(jsonPath("$.name").value("Test Group"))
                    .andExpect(jsonPath("$.userMember").value(true));
        }

        @Test
        @DisplayName("Should return 404 for non-existent group")
        void shouldReturn404ForNonExistentGroup() throws Exception {
            mockMvc.perform(get("/api/groups/{id}", 99999L)
                    .header("Authorization", "Bearer " + testUser1Token))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("Should search public groups")
        void shouldSearchPublicGroups() throws Exception {
            mockMvc.perform(get("/api/groups/search")
                    .param("q", "Test")
                    .param("page", "0")
                    .param("size", "10")
                    .header("Authorization", "Bearer " + testUser1Token))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content").isArray())
                    .andExpect(jsonPath("$.totalElements").value(1));
        }

        @Test
        @DisplayName("Should get public groups")
        void shouldGetPublicGroups() throws Exception {
            mockMvc.perform(get("/api/groups/public")
                    .param("page", "0")
                    .param("size", "10")
                    .header("Authorization", "Bearer " + testUser1Token))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content").isArray());
        }
    }

    @Nested
    @DisplayName("Group Membership API Tests")
    class GroupMembershipApiTests {

        private Group publicGroup;
        private Group privateGroup;

        @BeforeEach
        void setUpGroups() {
            publicGroup = createTestGroupWithPrivacy(Group.PrivacySetting.PUBLIC);
            privateGroup = createTestGroupWithPrivacy(Group.PrivacySetting.PRIVATE);
        }

        private Group createTestGroupWithPrivacy(Group.PrivacySetting privacy) {
            Group group = new Group();
            group.setName("Test Group " + privacy);
            group.setDescription("Test Description");
            group.setPrivacySetting(privacy);
            group.setCreatedBy(testUser1);
            group.setMaxMembers(50);
            group.setActive(true);
            group.setCreatedAt(LocalDateTime.now());
            group.setUpdatedAt(LocalDateTime.now());
            
            Group savedGroup = groupRepository.save(group);
            
            // Create owner membership
            GroupMembership ownerMembership = new GroupMembership();
            ownerMembership.setGroup(savedGroup);
            ownerMembership.setUser(testUser1);
            ownerMembership.setRole(GroupMembership.MembershipRole.OWNER);
            ownerMembership.setStatus(GroupMembership.MembershipStatus.ACTIVE);
            ownerMembership.setJoinedAt(LocalDateTime.now());
            ownerMembership.setCreatedAt(LocalDateTime.now());
            ownerMembership.setUpdatedAt(LocalDateTime.now());
            membershipRepository.save(ownerMembership);
            
            return savedGroup;
        }

        @Test
        @DisplayName("Should allow joining public group")
        void shouldJoinPublicGroup() throws Exception {
            mockMvc.perform(post("/api/groups/{id}/join", publicGroup.getId())
                    .header("Authorization", "Bearer " + testUser2Token))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.status").value("ACTIVE"));
        }

        @Test
        @DisplayName("Should create pending request for private group")
        void shouldCreatePendingRequestForPrivateGroup() throws Exception {
            mockMvc.perform(post("/api/groups/{id}/join", privateGroup.getId())
                    .header("Authorization", "Bearer " + testUser2Token))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.status").value("PENDING"));
        }

        @Test
        @DisplayName("Should allow leaving group")
        void shouldLeaveGroup() throws Exception {
            // First join the group
            mockMvc.perform(post("/api/groups/{id}/join", publicGroup.getId())
                    .header("Authorization", "Bearer " + testUser2Token))
                    .andExpect(status().isCreated());

            // Then leave the group
            mockMvc.perform(post("/api/groups/{id}/leave", publicGroup.getId())
                    .header("Authorization", "Bearer " + testUser2Token))
                    .andExpect(status().isNoContent());
        }

        @Test
        @DisplayName("Should get group members")
        void shouldGetGroupMembers() throws Exception {
            mockMvc.perform(get("/api/groups/{id}/members", publicGroup.getId())
                    .param("page", "0")
                    .param("size", "10")
                    .header("Authorization", "Bearer " + testUser1Token))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content").isArray())
                    .andExpect(jsonPath("$.totalElements").value(1));
        }
    }

    @Nested
    @DisplayName("Group Update API Tests")
    class GroupUpdateApiTests {

        private Group testGroup;

        @BeforeEach
        void setUpGroup() {
            testGroup = createTestGroup();
        }

        private Group createTestGroup() {
            Group group = new Group();
            group.setName("Test Group");
            group.setDescription("Test Description");
            group.setPrivacySetting(Group.PrivacySetting.PUBLIC);
            group.setCreatedBy(testUser1);
            group.setMaxMembers(50);
            group.setActive(true);
            group.setCreatedAt(LocalDateTime.now());
            group.setUpdatedAt(LocalDateTime.now());
            
            Group savedGroup = groupRepository.save(group);
            
            // Create owner membership
            GroupMembership ownerMembership = new GroupMembership();
            ownerMembership.setGroup(savedGroup);
            ownerMembership.setUser(testUser1);
            ownerMembership.setRole(GroupMembership.MembershipRole.OWNER);
            ownerMembership.setStatus(GroupMembership.MembershipStatus.ACTIVE);
            ownerMembership.setJoinedAt(LocalDateTime.now());
            ownerMembership.setCreatedAt(LocalDateTime.now());
            ownerMembership.setUpdatedAt(LocalDateTime.now());
            membershipRepository.save(ownerMembership);
            
            return savedGroup;
        }

        @Test
        @DisplayName("Should update group successfully by owner")
        void shouldUpdateGroupByOwner() throws Exception {
            UpdateGroupRequest request = new UpdateGroupRequest();
            request.setName("Updated Group Name");
            request.setDescription("Updated description");

            mockMvc.perform(put("/api/groups/{id}", testGroup.getId())
                    .header("Authorization", "Bearer " + testUser1Token)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.name").value("Updated Group Name"))
                    .andExpect(jsonPath("$.description").value("Updated description"));
        }

        @Test
        @DisplayName("Should reject update by non-member")
        void shouldRejectUpdateByNonMember() throws Exception {
            UpdateGroupRequest request = new UpdateGroupRequest();
            request.setName("Updated Group Name");

            mockMvc.perform(put("/api/groups/{id}", testGroup.getId())
                    .header("Authorization", "Bearer " + testUser2Token)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("Should delete group successfully by owner")
        void shouldDeleteGroupByOwner() throws Exception {
            mockMvc.perform(delete("/api/groups/{id}", testGroup.getId())
                    .header("Authorization", "Bearer " + testUser1Token))
                    .andExpect(status().isNoContent());
        }
    }
} 