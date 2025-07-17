package com.example.ballog.domain.emotion.service;

import com.example.ballog.domain.emotion.dto.request.EmotionEnrollRequest;
import com.example.ballog.domain.emotion.dto.request.EmotionRequest;
import com.example.ballog.domain.emotion.dto.response.EmotionEnrollResponse;
import com.example.ballog.domain.emotion.dto.response.EmotionResponse;
import com.example.ballog.domain.emotion.entity.Emotion;
import com.example.ballog.domain.emotion.entity.EmotionType;
import com.example.ballog.domain.emotion.repository.EmotionRepository;
import com.example.ballog.domain.match.entity.Matches;
import com.example.ballog.domain.matchrecord.entity.MatchRecord;
import com.example.ballog.domain.matchrecord.repository.MatchRecordRepository;
import com.example.ballog.global.common.exception.CustomException;
import com.example.ballog.global.common.exception.enums.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class EmotionService {

    private final EmotionRepository emotionRepository;
    private final MatchRecordRepository matchRecordRepository;

    public EmotionResponse createEmotion(EmotionEnrollRequest request, Long currentUserId) {

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

        emotionRepository.save(emotion);
        return getEmotionRatio(request.getRecordId(), currentUserId);
    }



    public EmotionResponse getEmotionRatio(Long recordId, Long currentUserId) {
        MatchRecord record = matchRecordRepository.findById(recordId)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_RECORD));

        if (!record.getUser().getUserId().equals(currentUserId)) {
            throw new CustomException(ErrorCode.RECORD_NOT_OWNED);
        }

        Matches matches = record.getMatches();

        List<Emotion> emotions = emotionRepository.findByMatchRecord(record);

        long positive = emotions.stream()
                .filter(e -> e.getEmotionType() == EmotionType.POSITIVE)
                .count();

        long negative = emotions.stream()
                .filter(e -> e.getEmotionType() == EmotionType.NEGATIVE)
                .count();

        long total = positive + negative;
        if (total == 0) total = 1;

        EmotionType recentEmotion = emotions.stream()
                .max(Comparator.comparing(Emotion::getCreatedAt))
                .map(Emotion::getEmotionType)
                .orElse(null);

        return EmotionResponse.builder()
                .matchesDate(matches.getMatchesDate())
                .matchesTime(matches.getMatchesTime())
                .homeTeam(matches.getHomeTeam())
                .awayTeam(matches.getAwayTeam())
                .stadium(matches.getStadium())
                .positivePercent((positive * 100.0) / total)
                .negativePercent((negative * 100.0) / total)
                .recentEmotion(recentEmotion)
                .defaultImageUrl(record.getDefaultImageUrl())
                .build();
    }

}
