package school.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "messages")
public class Message {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Sender and receiver reference the unique numeric user ID (User.id),
     * not the string userId field.
     */
    @Column(name = "sender_user_pk", nullable = false)
    private Long senderUserPk;

    @Column(name = "receiver_user_pk", nullable = false)
    private Long receiverUserPk;

    @Column(nullable = false, length = 2000)
    private String content;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private Boolean isRead;

    public Message() {
        this.createdAt = LocalDateTime.now();
        this.isRead = false;
    }

    public Message(Long senderUserPk, Long receiverUserPk, String content) {
        this.senderUserPk = senderUserPk;
        this.receiverUserPk = receiverUserPk;
        this.content = content;
        this.createdAt = LocalDateTime.now();
        this.isRead = false;
    }

    public Long getId() {
        return id;
    }

    public Long getSenderUserPk() {
        return senderUserPk;
    }

    public void setSenderUserPk(Long senderUserPk) {
        this.senderUserPk = senderUserPk;
    }

    public Long getReceiverUserPk() {
        return receiverUserPk;
    }

    public void setReceiverUserPk(Long receiverUserPk) {
        this.receiverUserPk = receiverUserPk;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public Boolean getIsRead() {
        return isRead;
    }

    public void setIsRead(Boolean isRead) {
        this.isRead = isRead;
    }
}