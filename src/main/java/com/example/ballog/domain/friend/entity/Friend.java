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
@Table(name = "friend",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"user_id", "friend_id"})
        })
public class Friend { // ACCEPTED 상태만 저장
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long friendId;

    @Column(name = "requester_id", nullable = false)
    private Long requesterId;

    @Column(name = "receiver_id", nullable = false)
    private Long receiverId;

}
