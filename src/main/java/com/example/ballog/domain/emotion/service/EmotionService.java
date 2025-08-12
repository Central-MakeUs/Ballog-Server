package com.example.ballog.domain.emotion.service;

import com.example.ballog.domain.emotion.dto.request.EmotionEnrollRequest;
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
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class EmotionService {

    private final EmotionRepository emotionRepository;
    private final MatchRecordRepository matchRecordRepository;

    public EmotionResponse createEmotion(EmotionEnrollRequest request, Long currentUserId) {
        MatchRecord matchRecord = findAndValidateMatchRecord(request.getMatchRecordId(), currentUserId);

        Emotion emotion = buildEmotion(request, matchRecord, currentUserId);
        emotionRepository.save(emotion);

        return getEmotionRatio(request.getMatchRecordId(), currentUserId);
    }

    /**
     * 연타 등록 가능 감정 표현 생성 (=> 새 트랜잭션으로 독립 처리)
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public EmotionResponse createEmotionNew(EmotionEnrollRequest request, Long currentUserId) {
        MatchRecord matchRecord = findAndValidateMatchRecord(request.getMatchRecordId(), currentUserId);

        Emotion emotion = buildEmotion(request, matchRecord, currentUserId);
        emotionRepository.saveAndFlush(emotion);

        return getEmotionRatio(request.getMatchRecordId(), currentUserId);
    }

    /**
     * 특정 기록에 대한 감정 비율 및 최신 감정 조회 => 직관기록 상세조회할 때 필요한 값들 계산
     */
    public EmotionResponse getEmotionRatio(Long recordId, Long currentUserId) {
        MatchRecord record = findAndValidateMatchRecord(recordId, currentUserId);

        List<Emotion> emotions = emotionRepository.findByMatchRecord(record);

        long positive = countEmotionByType(emotions, EmotionType.POSITIVE);
        long negative = countEmotionByType(emotions, EmotionType.NEGATIVE);
        long total = Math.max(positive + negative, 1);

        EmotionType recentEmotion = emotions.stream()
                .max(Comparator.comparing(Emotion::getCreatedAt))
                .map(Emotion::getEmotionType)
                .orElse(null);

        Matches matches = record.getMatches();

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

    private MatchRecord findAndValidateMatchRecord(Long recordId, Long currentUserId) {
        MatchRecord matchRecord = matchRecordRepository.findById(recordId)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_RECORD));

        if (!matchRecord.getUser().getUserId().equals(currentUserId)) {
            throw new CustomException(ErrorCode.RECORD_NOT_OWNED);
        }
        return matchRecord;
    }

    private Emotion buildEmotion(EmotionEnrollRequest request, MatchRecord matchRecord, Long currentUserId) {
        Emotion emotion = new Emotion();
        emotion.setMatchRecord(matchRecord);
        emotion.setMatches(matchRecord.getMatches());
        emotion.setUserId(currentUserId);
        emotion.setEmotionType(request.getEmotionType());
        emotion.setCreatedAt(LocalDateTime.now());
        return emotion;
    }

    private long countEmotionByType(List<Emotion> emotions, EmotionType type) {
        return emotions.stream()
                .filter(e -> e.getEmotionType() == type)
                .count();
    }
}
