package com.example.ballog.domain.matchrecord.repository;

import com.example.ballog.domain.login.entity.User;
import com.example.ballog.domain.matchrecord.entity.MatchRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MatchRecordRepository extends JpaRepository<MatchRecord, Long> {
    long countByUser(User user);
    List<MatchRecord> findAllByUserOrderByMatchrecordIdDesc(User user);
    List<MatchRecord> findAllByMatches_MatchesId(Long matchesId);
    Optional<MatchRecord> findByMatches_MatchesIdAndUser_UserId(Long matchesId, Long userId);


}
