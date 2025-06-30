package com.example.ballog.domain.login.dto.request;

import com.example.ballog.domain.login.entity.BaseballTeam;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SignupRequest {
    private String email;
    private String nickname;
    private BaseballTeam baseballTeam;
}