package com.example.ballog.domain.friend.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class FriendRedisRepository {
    private final StringRedisTemplate redisTemplate;

    private String requestKey(Long userId){
        return "friend:request:" + userId;
    }

    private String friendKey(Long userId){
        return "friend:list:" + userId;
    }

    // 친구 요청 추가
    public void addRequest(Long receiverId, Long requesterId){
        redisTemplate.opsForSet()
                .add(requestKey(receiverId), requesterId.toString());
    }

    // 요청 존재 여부
    public boolean hasRequest(Long receiverId, Long requesterId){
        return Boolean.TRUE.equals(
                redisTemplate.opsForSet()
                        .isMember(requestKey(receiverId), requesterId.toString())
        );
    }

    // 요청 삭제
    public void removeRequest(Long receiverId, Long requesterId){
        redisTemplate.opsForSet()
                .remove(requestKey(receiverId), requesterId.toString());
    }

    // 친구 추가 캐시
    public void addFriend(Long userId, Long friendId){
        redisTemplate.opsForSet()
                .add(friendKey(userId), friendId.toString());
    }

}
