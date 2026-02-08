package com.hdtpt.pentachat.message.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import jakarta.persistence.Column;
import java.time.LocalDateTime;

/**
 * Message entity supporting both personal and group messages
 * Type field indicates: PERSONAL (1-to-1) or GROUP (1-to-many)
 * TargetId: userId for PERSONAL, groupId for GROUP
 */
@Entity
@Table(name = "messages")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Message {

    public enum MessageType {
        PERSONAL,  // 1-to-1 message
        GROUP      // Group message
    }

    @Id
    private String id;

    @Column(nullable = false)
    private String fromUserId;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private MessageType type;

    @Column(nullable = false)
    private String targetId;  // userId for PERSONAL, groupId for GROUP

    @Column(nullable = false, columnDefinition = "NVARCHAR(MAX)")
    private String content;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @Column(nullable = false)
    private Boolean isRead;

    // Legacy field for backward compatibility
    @Column
    private String toUserId;
}
