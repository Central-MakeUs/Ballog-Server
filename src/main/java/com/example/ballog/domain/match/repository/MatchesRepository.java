package com.example.ballog.domain.match.repository;

import com.example.ballog.domain.match.entity.Matches;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface MatchesRepository extends JpaRepository<Matches, Long> {
    List<Matches> findAllByMatchesDate(LocalDate date);
    // 오늘 이후 경기 조회 (>= today)
    List<Matches> findAllByMatchesDateGreaterThanEqual(LocalDate date);

    @Query("SELECT m FROM Matches m WHERE m.matchesDate >= :today AND (m.startAlertScheduled = false OR m.inGameAlertScheduled = false)")
    List<Matches> findUpcomingMatchesWithNoAlerts(@Param("today") LocalDate today);

    @Modifying
    @Query("UPDATE Matches m SET m.startAlertScheduled = true, m.inGameAlertScheduled = true WHERE m.matchesId = :matchId")
    void markAlertsScheduled(@Param("matchId") Long matchId);

    List<Matches> findByMatchesDate(LocalDate matchesDate);


}
