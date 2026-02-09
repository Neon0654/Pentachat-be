package com.hdtpt.pentachat.friend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hdtpt.pentachat.friend.dto.FriendRequestDTO;
import com.hdtpt.pentachat.friend.model.FriendRequest;
import com.hdtpt.pentachat.friend.service.FriendService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration test cho FriendController
 */
@SpringBootTest
@AutoConfigureMockMvc
@DisplayName("Friend Controller Tests")
class FriendControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private FriendService friendService;

    private FriendRequest mockFriendRequest;
    private FriendRequestDTO mockDTO;

    @BeforeEach
    void setUp() {
        String requestId = UUID.randomUUID().toString();
        String userId1 = UUID.randomUUID().toString();
        String userId2 = UUID.randomUUID().toString();

        mockFriendRequest = FriendRequest.builder()
                .id(requestId)
                .fromUserId(userId1)
                .toUserId(userId2)
                .status("PENDING")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        mockDTO = FriendRequestDTO.builder()
                .id(requestId)
                .fromUserId(userId1)
                .toUserId(userId2)
                .status("PENDING")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    @Test
    @DisplayName("POST /api/friends/request - Send friend request successfully")
    void testSendFriendRequest_Success() throws Exception {
        // Given
        FriendRequestDTO requestDTO = FriendRequestDTO.builder()
                .fromUserId(mockDTO.getFromUserId())
                .toUserId(mockDTO.getToUserId())
                .build();

        when(friendService.sendFriendRequest(anyString(), anyString()))
                .thenReturn(mockFriendRequest);

        // When & Then
        mockMvc.perform(post("/api/friends/request")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.message", containsString("Friend request sent successfully")))
                .andExpect(jsonPath("$.data.id", notNullValue()))
                .andExpect(jsonPath("$.data.status", is("PENDING")));

        verify(friendService, times(1)).sendFriendRequest(anyString(), anyString());
    }

    @Test
    @DisplayName("POST /api/friends/request - Invalid request (missing fields)")
    void testSendFriendRequest_InvalidRequest() throws Exception {
        // Given
        FriendRequestDTO invalidDTO = FriendRequestDTO.builder()
                .fromUserId("") // Empty
                .toUserId(mockDTO.getToUserId())
                .build();

        // When & Then
        mockMvc.perform(post("/api/friends/request")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidDTO)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /api/friends/request - Friend request already sent")
    void testSendFriendRequest_AlreadySent() throws Exception {
        // Given
        FriendRequestDTO requestDTO = FriendRequestDTO.builder()
                .fromUserId(mockDTO.getFromUserId())
                .toUserId(mockDTO.getToUserId())
                .build();

        when(friendService.sendFriendRequest(anyString(), anyString()))
                .thenThrow(new IllegalArgumentException("Friend request already sent"));

        // When & Then
        mockMvc.perform(post("/api/friends/request")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success", is(false)))
                .andExpect(jsonPath("$.message", containsString("Friend request already sent")));
    }

    @Test
    @DisplayName("POST /api/friends/accept/{requestId} - Accept friend request successfully")
    void testAcceptFriend_Success() throws Exception {
        // Given
        FriendRequest acceptedRequest = FriendRequest.builder()
                .id(mockFriendRequest.getId())
                .fromUserId(mockFriendRequest.getFromUserId())
                .toUserId(mockFriendRequest.getToUserId())
                .status("ACCEPTED")
                .createdAt(mockFriendRequest.getCreatedAt())
                .updatedAt(LocalDateTime.now())
                .build();

        when(friendService.acceptFriend(mockFriendRequest.getId()))
                .thenReturn(acceptedRequest);

        // When & Then
        mockMvc.perform(post("/api/friends/accept/{requestId}", mockFriendRequest.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.message", containsString("Friend request accepted successfully")))
                .andExpect(jsonPath("$.data.status", is("ACCEPTED")));

        verify(friendService, times(1)).acceptFriend(mockFriendRequest.getId());
    }

    @Test
    @DisplayName("POST /api/friends/accept/{requestId} - Request not found")
    void testAcceptFriend_NotFound() throws Exception {
        // Given
        String invalidId = "invalid-id";
        when(friendService.acceptFriend(invalidId))
                .thenThrow(new IllegalArgumentException("Friend request not found"));

        // When & Then
        mockMvc.perform(post("/api/friends/accept/{requestId}", invalidId))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success", is(false)))
                .andExpect(jsonPath("$.message", containsString("Friend request not found")));
    }

    @Test
    @DisplayName("POST /api/friends/reject/{requestId} - Reject friend request successfully")
    void testRejectFriend_Success() throws Exception {
        // Given
        FriendRequest rejectedRequest = FriendRequest.builder()
                .id(mockFriendRequest.getId())
                .fromUserId(mockFriendRequest.getFromUserId())
                .toUserId(mockFriendRequest.getToUserId())
                .status("REJECTED")
                .createdAt(mockFriendRequest.getCreatedAt())
                .updatedAt(LocalDateTime.now())
                .build();

        when(friendService.rejectFriend(mockFriendRequest.getId()))
                .thenReturn(rejectedRequest);

        // When & Then
        mockMvc.perform(post("/api/friends/reject/{requestId}", mockFriendRequest.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.message", containsString("Friend request rejected successfully")))
                .andExpect(jsonPath("$.data.status", is("REJECTED")));

        verify(friendService, times(1)).rejectFriend(mockFriendRequest.getId());
    }

    @Test
    @DisplayName("GET /api/friends/pending/{userId} - Get pending requests successfully")
    void testGetPendingRequests_Success() throws Exception {
        // Given
        String userId = mockDTO.getToUserId();
        List<FriendRequest> pendingRequests = Arrays.asList(mockFriendRequest);

        when(friendService.getPendingRequests(userId))
                .thenReturn(pendingRequests);

        // When & Then
        mockMvc.perform(get("/api/friends/pending/{userId}", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.message", containsString("Pending requests retrieved successfully")))
                .andExpect(jsonPath("$.data", hasSize(1)))
                .andExpect(jsonPath("$.data[0].status", is("PENDING")));

        verify(friendService, times(1)).getPendingRequests(userId);
    }

    @Test
    @DisplayName("GET /api/friends/pending/{userId} - Empty pending requests")
    void testGetPendingRequests_Empty() throws Exception {
        // Given
        String userId = UUID.randomUUID().toString();
        when(friendService.getPendingRequests(userId))
                .thenReturn(Arrays.asList());

        // When & Then
        mockMvc.perform(get("/api/friends/pending/{userId}", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data", hasSize(0)));
    }

    @Test
    @DisplayName("GET /api/friends/check/{userId1}/{userId2} - Check friendship - are friends")
    void testCheckFriendship_AreFriends() throws Exception {
        // Given
        String userId1 = mockDTO.getFromUserId();
        String userId2 = mockDTO.getToUserId();

        when(friendService.areFriends(userId1, userId2))
                .thenReturn(true);

        // When & Then
        mockMvc.perform(get("/api/friends/check/{userId1}/{userId2}", userId1, userId2))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.message", containsString("Friendship status retrieved successfully")))
                .andExpect(jsonPath("$.data", is(true)));

        verify(friendService, times(1)).areFriends(userId1, userId2);
    }

    @Test
    @DisplayName("GET /api/friends/check/{userId1}/{userId2} - Check friendship - not friends")
    void testCheckFriendship_NotFriends() throws Exception {
        // Given
        String userId1 = UUID.randomUUID().toString();
        String userId2 = UUID.randomUUID().toString();

        when(friendService.areFriends(userId1, userId2))
                .thenReturn(false);

        // When & Then
        mockMvc.perform(get("/api/friends/check/{userId1}/{userId2}", userId1, userId2))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data", is(false)));
    }
}
