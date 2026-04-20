package com.example.ballog.domain.friend.service;

import com.example.ballog.domain.alert.service.AlertService;
import com.example.ballog.domain.emotion.repository.EmotionRepository;
import com.example.ballog.domain.friend.dto.response.FriendRequestResponse;
import com.example.ballog.domain.friend.dto.response.FriendResponse;
import com.example.ballog.domain.friend.entity.Friend;
import com.example.ballog.domain.friend.entity.FriendRequest;
import com.example.ballog.domain.friend.entity.FriendStatus;
import com.example.ballog.domain.friend.repository.FriendRepository;
import com.example.ballog.domain.friend.repository.FriendRequestRepository;
import com.example.ballog.domain.login.entity.User;
import com.example.ballog.domain.login.repository.UserRepository;
import com.example.ballog.global.common.exception.CustomException;
import com.example.ballog.global.common.exception.enums.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class FriendService {
    private final UserRepository userRepository;
    private final FriendRepository friendRepository;
    private final FriendRequestRepository friendRequestRepository;
    private final EmotionRepository emotionRepository;
    private final AlertService alertService;

    // 친구요청-> 거절 -> 재요청 가능
    @Transactional
    public void requestFriend(Long requesterId, String nickname) {

        User receiver = userRepository.findByNickname(nickname).orElseThrow(() -> new CustomException(ErrorCode.INVALID_USER));
        Long receiverId = receiver.getUserId();

        // 자기 자신 요청 방지
        if (requesterId.equals(receiverId)) {
            throw new CustomException(ErrorCode.FRIEND_REQUEST_NOT_ALLOWED);
        }

        // 이미 친구인지
        if (friendRepository.existsByRequesterIdAndReceiverId(requesterId, receiverId)) {
            throw new CustomException(ErrorCode.ALREADY_FRIEND);
        }

        Optional<FriendRequest> existing = friendRequestRepository.findByRequesterIdAndReceiverId(requesterId, receiverId);

        if (existing.isPresent()) {
            FriendRequest request = existing.get();

            if (request.getStatus() == FriendStatus.PENDING) {
                throw new CustomException(ErrorCode.ALREADY_REQUESTED);
            }

            if (request.getStatus() == FriendStatus.ACCEPTED) {
                throw new CustomException(ErrorCode.ALREADY_FRIEND);
            }

            if (request.getStatus() == FriendStatus.REJECTED) {
                // 재요청 허용
                request.setStatus(FriendStatus.PENDING);

                // 알림 다시 보내기
                alertService.sendFriendRequestNotification(requesterId, receiverId);
                return;
            }
        }

        // 역방향 요청 확인 (receiver → requester)
        Optional<FriendRequest> reverseRequest =
                friendRequestRepository.findByRequesterIdAndReceiverId(receiverId, requesterId);

        if (reverseRequest.isPresent()) {
            FriendRequest request = reverseRequest.get();

            if (request.getStatus() == FriendStatus.PENDING) {
                acceptFriend(requesterId, receiverId);
                return;
            }

            if (request.getStatus() == FriendStatus.REJECTED) {
                request.setRequesterId(requesterId);
                request.setReceiverId(receiverId);
                request.setStatus(FriendStatus.PENDING);

                alertService.sendFriendRequestNotification(requesterId, receiverId);
                return;
            }
        }

        // 신규 요청 생성
        friendRequestRepository.save(new FriendRequest(null, requesterId, receiverId, FriendStatus.PENDING));

        // 알림 전송
        alertService.sendFriendRequestNotification(requesterId, receiverId);
    }

    // 친구 요청 수락
    @Transactional
    public void acceptFriend(Long userId, Long requesterId) {

        FriendRequest request = friendRequestRepository
                .findByRequesterIdAndReceiverId(requesterId, userId)
                .orElseThrow(() -> new CustomException(ErrorCode.FRIEND_REQUEST_NOT_FOUND));

        if (request.getStatus() != FriendStatus.PENDING) {
            throw new CustomException(ErrorCode.INVALID_FRIEND_REQUEST);
        }

        // 상태 변경
        request.accept();

        // 친구 관계 생성 (양방향)
        friendRepository.save(new Friend(null, userId, requesterId));
        friendRepository.save(new Friend(null, requesterId, userId));
    }

    // 친구 요청 거절
    @Transactional
    public void rejectFriend(Long receiverId, Long requesterId) {

        FriendRequest request = friendRequestRepository
                .findByRequesterIdAndReceiverId(requesterId, receiverId)
                .orElseThrow(() -> new CustomException(ErrorCode.FRIEND_REQUEST_NOT_FOUND));

        if (request.getStatus() != FriendStatus.PENDING) {
            throw new CustomException(ErrorCode.INVALID_FRIEND_REQUEST);
        }

        request.reject();
    }


    // 친구 요청 목록 조회
    public List<FriendRequestResponse> getReceivedRequests(Long userId) {
        List<FriendRequest> requests = friendRequestRepository.findAllByReceiverIdAndStatus(userId, FriendStatus.PENDING);

        List<Long> requesterIds = requests.stream()
                .map(FriendRequest::getRequesterId)
                .toList();

        Map<Long, User> userMap = userRepository.findAllById(requesterIds)
                .stream()
                .collect(Collectors.toMap(User::getUserId, u -> u));

        // 감정 Map 추가
        Map<Long, String> emotionMap =
                emotionRepository.countEmotionByUserIds(requesterIds)
                        .stream()
                        .collect(Collectors.toMap(
                                EmotionRepository.EmotionCountProjection::getUserId,
                                e -> calculateEmotion(e.getPositiveCnt(), e.getNegativeCnt())
                        ));




        return requests.stream()
                .map(req -> {
                    User user = userMap.get(req.getRequesterId());

                    return new FriendRequestResponse(
                            req.getRequesterId(),
                            user.getNickname(),
                            user.getBaseballTeam().name(),
                            emotionMap.getOrDefault(req.getRequesterId(), "NEUTRAL")
                    );
                })
                .toList();
    }

    // 친구 목록 조회
    @Transactional(readOnly = true)
    public List<FriendResponse> getFriends(Long userId) {

        List<Friend> friends =
                friendRepository.findAllByRequesterIdOrReceiverId(userId, userId);

        List<Long> friendIds = friends.stream()
                .map(friend -> friend.getRequesterId().equals(userId) ? friend.getReceiverId() : friend.getRequesterId())
                .distinct()
                .toList();

        if (friendIds.isEmpty()) return List.of();

        List<User> users = userRepository.findByUserIdIn(friendIds);

        Map<Long, String> emotionMap =
                emotionRepository.countEmotionByUserIds(friendIds)
                        .stream()
                        .collect(Collectors.toMap(
                                EmotionRepository.EmotionCountProjection::getUserId,
                                e -> calculateEmotion(e.getPositiveCnt(), e.getNegativeCnt())
                        ));


        return users.stream()
                .map(u -> new FriendResponse(
                        u.getUserId(),
                        u.getNickname(),
                        u.getBaseballTeam().name(),
                        emotionMap.getOrDefault(u.getUserId(), "NEUTRAL")
                ))
                .toList();

    }

    // 감정 계산
    private String calculateEmotion(Long positive, Long negative) {
        long p = positive == null ? 0 : positive;
        long n = negative == null ? 0 : negative;
        long total = p + n;

        if (total == 0) return "NEUTRAL";

        double ratio = (double) p / total;

        if (ratio >= 0.6) return "POSITIVE";
        if (ratio <= 0.4) return "NEGATIVE";
        return "NEUTRAL";
    }


}
