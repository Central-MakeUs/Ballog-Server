package com.example.ballog.domain.match.repository;

import com.example.ballog.domain.match.entity.Matches;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface MatchesRepository extends JpaRepository<Matches, Long> {
    List<Matches> findAllByMatchesDate(LocalDate date);
    // 오늘 이후 경기 조회 (>= today)
    List<Matches> findAllByMatchesDateGreaterThanEqual(LocalDate date);

}
