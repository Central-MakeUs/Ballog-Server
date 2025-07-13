package com.example.ballog.domain.matchrecord.entity;

import com.example.ballog.domain.login.entity.BaseballTeam;
import com.example.ballog.domain.login.entity.User;
import com.example.ballog.domain.match.entity.Matches;
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
@Table(name = "match_record", schema = "ballog")
public class MatchRecord {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long matchrecordId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "match_id")
    private Matches matches;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    private Long watchCnt;

    @Enumerated(EnumType.STRING)
    private Result result;

    @Enumerated(EnumType.STRING)
    private BaseballTeam baseballTeam;

    @Column(nullable = false)
    private boolean autoProcessed = false;

    @Column(nullable = false)
    private String defaultImageUrl;
}
