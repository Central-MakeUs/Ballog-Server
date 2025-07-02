package com.example.ballog.domain.login.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class UserInfoResponse {
    private Long userId;
    private String email;
    private String nickname;
    private String baseballTeam;
    private Boolean isNewUser;
    private String role;
}