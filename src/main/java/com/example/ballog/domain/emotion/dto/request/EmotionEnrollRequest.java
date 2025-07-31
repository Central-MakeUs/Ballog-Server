package com.example.ballog.domain.emotion.dto.request;

import com.example.ballog.domain.emotion.entity.EmotionType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class EmotionEnrollRequest {
    private Long matchRecordId;
    private EmotionType emotionType;
}
