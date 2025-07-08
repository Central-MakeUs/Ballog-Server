package com.example.ballog.domain.emotion.repository;

import com.example.ballog.domain.emotion.entity.Emotion;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EmotionRepository extends JpaRepository<Emotion, Long> {
}
