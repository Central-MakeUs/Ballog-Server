package com.example.ballog.domain.friend.repository;

import com.example.ballog.domain.friend.entity.FriendRequest;
import com.example.ballog.domain.friend.entity.FriendStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FriendRequestRepository extends JpaRepository<FriendRequest, Long> {

    Optional<FriendRequest> findByRequesterIdAndReceiverId(Long requesterId, Long receiverId);

    boolean existsByRequesterIdAndReceiverIdAndStatus(Long requesterId, Long receiverId, FriendStatus status);
    List<FriendRequest> findAllByReceiverIdAndStatus(Long receiverId, FriendStatus status);
}