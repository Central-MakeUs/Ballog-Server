package com.example.ballog.domain.friend.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class FriendRequestResponse {
    private Long requesterId;
    private String nickname;

    private String baseballTeam;
    private String emotion; // POSITIVE / NEUTRAL / NEGATIVE
}
