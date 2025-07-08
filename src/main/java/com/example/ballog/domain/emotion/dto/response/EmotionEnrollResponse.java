package com.example.ballog.domain.emotion.dto.response;

import com.example.ballog.domain.emotion.entity.EmotionType;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class EmotionEnrollResponse {
    private Long emotionId;
    private EmotionType emotionType;
    private LocalDateTime createdAt;
    private Long recordId;
}
