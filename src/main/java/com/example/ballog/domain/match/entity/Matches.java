package com.example.ballog.domain.match.entity;

import com.example.ballog.domain.login.entity.BaseballTeam;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "matches", schema = "ballog")
public class Matches {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "matches_id")
    private Long matchesId;

    @Column(name = "matches_date", nullable = false)
    private LocalDate matchesDate;

    @Column(name = "matches_time", nullable = false)
    private LocalTime matchesTime;

    @Enumerated(EnumType.STRING)
    @Column(name = "home_team", nullable = false)
    private BaseballTeam homeTeam;

    @Enumerated(EnumType.STRING)
    @Column(name = "away_team", nullable = false)
    private BaseballTeam awayTeam;

    @Enumerated(EnumType.STRING)
    @Column(name = "stadium", nullable = false)
    private Stadium stadium;

    @Column(name = "matches_result", length = 100)
    private String matchesResult;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private Status status;

    @Column(name = "start_alert_scheduled", nullable = false)
    private boolean startAlertScheduled = false;

    @Column(name = "in_game_alert_scheduled", nullable = false)
    private boolean inGameAlertScheduled = false;

    @PrePersist
    public void prePersist() {
        if (status == null) {
            status = Status.DEFAULT;
        }
    }
}
