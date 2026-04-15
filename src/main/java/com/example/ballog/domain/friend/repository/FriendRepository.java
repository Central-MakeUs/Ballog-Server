package com.example.ballog.domain.friend.repository;

import com.example.ballog.domain.friend.entity.Friend;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FriendRepository extends JpaRepository<Friend, Long> {

    List<Friend> findAllByRequesterIdOrReceiverId(Long requesterId, Long receiverId);
    boolean existsByRequesterIdAndReceiverId(Long requesterId, Long receiverId);

}
