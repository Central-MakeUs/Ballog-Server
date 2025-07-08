package com.example.ballog.domain.emotion.service;

import com.example.ballog.domain.emotion.dto.request.EmotionRequest;
import com.example.ballog.domain.emotion.dto.response.EmotionResponse;
import com.example.ballog.domain.emotion.entity.Emotion;
import com.example.ballog.domain.emotion.repository.EmotionRepository;
import com.example.ballog.domain.match.entity.Matches;
import com.example.ballog.domain.matchrecord.entity.MatchRecord;
import com.example.ballog.domain.matchrecord.repository.MatchRecordRepository;
import com.example.ballog.global.common.exception.CustomException;
import com.example.ballog.global.common.exception.enums.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class EmotionService {

    private final EmotionRepository emotionRepository;
    private final MatchRecordRepository matchRecordRepository;

    public EmotionResponse createEmotion(EmotionRequest request, Long currentUserId) {

        MatchRecord matchRecord = matchRecordRepository.findById(request.getRecordId())
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_RECORD));


        if (!matchRecord.getUser().getUserId().equals(currentUserId)) {
            throw new CustomException(ErrorCode.RECORD_NOT_OWNED);
        }

        Matches matches = matchRecord.getMatches();

        Emotion emotion = new Emotion();
        emotion.setMatchRecord(matchRecord);
        emotion.setMatches(matches);
        emotion.setUserId(currentUserId);
        emotion.setEmotionType(request.getEmotionType());
        emotion.setCreatedAt(LocalDateTime.now());

        Emotion saved = emotionRepository.save(emotion);
        return EmotionResponse.builder()
                .emotionId(saved.getEmotionId())
                .emotionType(saved.getEmotionType())
                .createdAt(saved.getCreatedAt())
                .recordId(saved.getMatchRecord().getMatchrecordId())
                .build();
    }
}
