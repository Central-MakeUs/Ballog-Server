package com.example.ballog.domain.friend.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "friend_request",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"requester_id", "receiver_id"})
        })
public class FriendRequest {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "requester_id", nullable = false)
    private Long requesterId;

    @Column(name = "receiver_id", nullable = false)
    private Long receiverId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private FriendStatus status;

    public void accept() {
        this.status = FriendStatus.ACCEPTED;
    }

    public void reject() {
        this.status = FriendStatus.REJECTED;
    }


}
