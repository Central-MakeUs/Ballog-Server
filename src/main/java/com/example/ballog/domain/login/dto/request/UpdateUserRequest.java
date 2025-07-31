package com.example.ballog.domain.login.dto.request;

import com.example.ballog.domain.login.entity.BaseballTeam;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UpdateUserRequest {
    private String nickname;
    private BaseballTeam baseballTeam;
}
