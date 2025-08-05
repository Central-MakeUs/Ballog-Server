package com.example.ballog.domain.emotion.repository;

import com.example.ballog.domain.emotion.entity.Emotion;
import com.example.ballog.domain.matchrecord.entity.MatchRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EmotionRepository extends JpaRepository<Emotion, Long> {
    List<Emotion> findByMatchRecord(MatchRecord matchRecord);
    @Query("SELECT e FROM Emotion e WHERE e.matchRecord.matchrecordId = :recordId")
    List<Emotion> findByMatchRecordId(@Param("recordId") Long recordId);

    List<Emotion> findByUserId(Long userId);

    @Modifying
    @Query("delete from Emotion e where e.matchRecord.user.userId = :userId")
    void deleteAllByUserUserId(@Param("userId") Long userId);
}
