package com.hdtpt.pentachat.groups.service;

import com.hdtpt.pentachat.dataaccess.DataApi;
import com.hdtpt.pentachat.groups.model.Group;
import com.hdtpt.pentachat.util.IdGenerator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class GroupService {

    private final DataApi dataApi;

    public GroupService(DataApi dataApi) {
        this.dataApi = dataApi;
    }

    /**
     * Tạo một nhóm chat mới
     */
    public Group createGroup(String name, String creatorId, List<String> memberIds) {
        // 1. Validate dữ liệu đầu vào
        if (name == null || name.trim().isEmpty()) {
            throw new RuntimeException("Tên nhóm không được để trống");
        }

        // 2. Đảm bảo người tạo luôn có tên trong danh sách thành viên
        List<String> finalMembers = new ArrayList<>(memberIds != null ? memberIds : new ArrayList<>());
        if (!finalMembers.contains(creatorId)) {
            finalMembers.add(creatorId);
        }

        // 3. Khởi tạo đối tượng Group
        LocalDateTime now = LocalDateTime.now();
        Group newGroup = Group.builder()
                .id(IdGenerator.generateId())
                .name(name)
                .creatorId(creatorId)
                .memberIds(finalMembers)
                .createdAt(now)
                .build();

        log.info("Đang tạo nhóm mới: {} bởi user {}", name, creatorId);

        // 4. LƯU VÀO DATABASE (Đã bỏ comment)
        return dataApi.saveGroup(newGroup);
    }

    /**
     * Lấy danh sách các nhóm mà user tham gia
     */
    public List<Group> getUserGroups(String userId) {
        log.info("Đang lấy danh sách nhóm cho user: {}", userId);

        // 5. GỌI DATA API ĐỂ LẤY DỮ LIỆU THẬT (Đã bỏ comment)
        return dataApi.findGroupsByUserId(userId);
    }

    /**
     * Save or update a group (from JpaDataApiImpl)
     * 
     * @param group Group entity to save
     * @return Saved group entity
     */
    public Group saveGroup(Group group) {
        return dataApi.saveGroup(group);
    }

    /**
     * Find group by ID (from JpaDataApiImpl)
     * 
     * @param groupId Group ID to search for
     * @return Group entity or null if not found
     */
    public Group findGroupById(String groupId) {
        return dataApi.findGroupById(groupId);
    }

    /**
     * Find all groups that a user is a member of (from JpaDataApiImpl)
     * 
     * @param userId User ID to search for
     * @return List of groups the user belongs to
     */
    public List<Group> findGroupsByUserId(String userId) {
        return dataApi.findGroupsByUserId(userId);
    }
}