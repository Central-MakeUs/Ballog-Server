package com.example.ballog.domain.login.entity;

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
@Table(name = "user", schema = "ballog")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long userId;

    @Column(name = "kakao_id")
    private Long kakaoId;  //카카오 유저 고유 ID -> 회원탈퇴 시 필요

    @Column(nullable = false, unique = true)
    private String email;

    @Column(length = 10, unique = true, nullable = true)
    private String nickname;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BaseballTeam baseballTeam = BaseballTeam.NONE;

    @Column(nullable = false)
    private Boolean isNewUser = true;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role = Role.USER;

}
