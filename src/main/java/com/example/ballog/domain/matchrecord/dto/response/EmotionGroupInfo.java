package com.example.ballog.domain.matchrecord.dto.response;

import com.example.ballog.domain.emotion.entity.EmotionType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor
public class EmotionGroupInfo {
    private LocalDateTime groupStart; // 1분 단위로 감정 그룹핑
    private EmotionType emotionType;
    private long count;
}
