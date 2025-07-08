package com.example.ballog.domain.emotion.repository;

import com.example.ballog.domain.emotion.entity.Emotion;
import com.example.ballog.domain.matchrecord.entity.MatchRecord;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface EmotionRepository extends JpaRepository<Emotion, Long> {
    List<Emotion> findByMatchRecord(MatchRecord matchRecord);
}
