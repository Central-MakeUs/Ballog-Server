package com.example.ballog.domain.matchrecord.repository;

import com.example.ballog.domain.login.entity.User;
import com.example.ballog.domain.matchrecord.entity.MatchRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MatchRecordRepository extends JpaRepository<MatchRecord, Long> {
    long countByUser(User user);
    List<MatchRecord> findAllByResultIsNull();
    List<MatchRecord> findAllByUserOrderByMatchrecordIdDesc(User user);
}
