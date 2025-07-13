package com.example.ballog.domain.matchrecord.service;

import com.example.ballog.domain.emotion.entity.Emotion;
import com.example.ballog.domain.emotion.entity.EmotionType;
import com.example.ballog.domain.emotion.repository.EmotionRepository;
import com.example.ballog.domain.login.entity.User;
import com.example.ballog.domain.match.entity.Matches;
import com.example.ballog.domain.match.repository.MatchesRepository;
import com.example.ballog.domain.matchrecord.dto.request.MatchRecordRequest;
import com.example.ballog.domain.matchrecord.dto.response.MatchRecordListResponse;
import com.example.ballog.domain.matchrecord.dto.response.MatchRecordResponse;
import com.example.ballog.domain.matchrecord.dto.response.MatchRecordSummaryResponse;
import com.example.ballog.domain.matchrecord.entity.MatchRecord;
import com.example.ballog.domain.matchrecord.entity.Result;
import com.example.ballog.domain.matchrecord.repository.MatchRecordRepository;
import com.example.ballog.global.common.exception.CustomException;
import com.example.ballog.global.common.exception.enums.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MatchRecordService {
    private final MatchesRepository matchesRepository;
    private final MatchRecordRepository matchRecordRepository;
    private final EmotionRepository emotionRepository;
    @Value("${app.default-image-url}")
    private String defaultImageUrl;

    @Transactional
    public MatchRecordResponse createRecord(MatchRecordRequest request, User user) {
        Matches matches = matchesRepository.findById(request.getMatchesId())
                .orElseThrow(() -> new CustomException(ErrorCode.MATCH_NOT_FOUND));

        long cnt= matchRecordRepository.countByUser(user);

        MatchRecord record = new MatchRecord();
        record.setMatches(matches);
        record.setUser(user);
        record.setWatchCnt(cnt+1);
        record.setResult(request.getResult());
        record.setBaseballTeam(user.getBaseballTeam());
        record.setDefaultImageUrl(defaultImageUrl);
        matchRecordRepository.save(record);


        return MatchRecordResponse.builder()
                .matchRecordId(record.getMatchrecordId())
                .matchesId(matches.getMatchesId())
                .homeTeam(matches.getHomeTeam().name())
                .awayTeam(matches.getAwayTeam().name())
                .matchDate(matches.getMatchesDate().toString())
                .matchTime(matches.getMatchesTime().toString())
                .userId(user.getUserId())
                .watchCnt(record.getWatchCnt())
                .result(record.getResult())
                .baseballTeam(record.getBaseballTeam())
                .defaultImageUrl(record.getDefaultImageUrl())
                .build();
    }

    @Transactional
    public void updateResult(Long recordId, Result result) {
        MatchRecord matchRecord = matchRecordRepository.findById(recordId)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_RECORD));

        matchRecord.setResult(result);
    }

    @Scheduled(fixedDelay = 60 * 60 * 1000)
    @Transactional
    public void autoUnfinishedRecords() {
        LocalDateTime now = LocalDateTime.now();

        List<MatchRecord> pendingRecords = matchRecordRepository.findAllByResultIsNull();

        for (MatchRecord record : pendingRecords) {
            Matches match = record.getMatches();
            LocalDateTime matchDateTime = LocalDateTime.of(
                    match.getMatchesDate(),
                    match.getMatchesTime()
            );

            if (matchDateTime.plusHours(8).isBefore(now)) {
                record.setResult(Result.SKIP);
            }
        }
    }

    @Transactional(readOnly = true)
    public MatchRecordResponse getRecordDetail(Long recordId, User currentUser) {
        MatchRecord record = matchRecordRepository.findById(recordId)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_RECORD));

        if (!record.getUser().getUserId().equals(currentUser.getUserId())) {
            throw new CustomException(ErrorCode.RECORD_NOT_OWNED);
        }

        Matches match = record.getMatches();

        List<Emotion> emotions = emotionRepository.findByMatchRecordId(recordId);
        long totalCount = emotions.size();
        long positiveCount = emotions.stream()
                .filter(e -> e.getEmotionType() == EmotionType.POSITIVE)
                .count();
        long negativeCount = emotions.stream()
                .filter(e -> e.getEmotionType() == EmotionType.NEGATIVE)
                .count();

        double positivePercent = totalCount == 0 ? 0.0 : (positiveCount * 100.0) / totalCount;
        double negativePercent = totalCount == 0 ? 0.0 : (negativeCount * 100.0) / totalCount;

        return MatchRecordResponse.builder()
                .matchRecordId(record.getMatchrecordId())
                .matchesId(match.getMatchesId())
                .homeTeam(match.getHomeTeam().name())
                .awayTeam(match.getAwayTeam().name())
                .matchDate(match.getMatchesDate().toString())
                .matchTime(match.getMatchesTime().toString())
                .userId(record.getUser().getUserId())
                .watchCnt(record.getWatchCnt())
                .result(record.getResult())
                .baseballTeam(record.getBaseballTeam())
                .positiveEmotionPercent(positivePercent)
                .negativeEmotionPercent(negativePercent)
                .build();
    }




    @Transactional(readOnly = true)
    public MatchRecordListResponse getAllRecordsByUser(User user) {
        List<MatchRecord> records = matchRecordRepository.findAllByUserOrderByMatchrecordIdDesc(user);
        int totalCount = records.size();

        List<MatchRecordSummaryResponse> recordResponses = records.stream().map(record -> {
            Matches match = record.getMatches();
            return MatchRecordSummaryResponse.builder()
                    .matchRecordId(record.getMatchrecordId())
                    .matchesId(match.getMatchesId())
                    .homeTeam(match.getHomeTeam().name())
                    .awayTeam(match.getAwayTeam().name())
                    .matchDate(match.getMatchesDate().toString())
                    .matchTime(match.getMatchesTime().toString())
                    .userId(record.getUser().getUserId())
                    .watchCnt(record.getWatchCnt())
                    .result(record.getResult())
                    .baseballTeam(record.getBaseballTeam())
                    .build();
        }).collect(Collectors.toList());


        long winCount = records.stream()
                .filter(r -> r.getResult() == Result.WIN)
                .count();
        double winRate = totalCount == 0 ? 0.0 : (winCount * 100.0) / totalCount;

        List<Emotion> allEmotions = emotionRepository.findByUserId(user.getUserId());
        long totalEmotionCount = allEmotions.size();

        long positiveCount = allEmotions.stream()
                .filter(e -> e.getEmotionType() == EmotionType.POSITIVE)
                .count();

        long negativeCount = allEmotions.stream()
                .filter(e -> e.getEmotionType() == EmotionType.NEGATIVE)
                .count();

        double positiveEmotionPercent = totalEmotionCount == 0 ? 0.0 : (positiveCount * 100.0) / totalEmotionCount;
        double negativeEmotionPercent = totalEmotionCount == 0 ? 0.0 : (negativeCount * 100.0) / totalEmotionCount;

        return MatchRecordListResponse.builder()
                .totalCount(totalCount)
                .winRate(winRate)
                .totalPositiveEmotionPercent(positiveEmotionPercent)
                .totalNegativeEmotionPercent(negativeEmotionPercent)
                .records(recordResponses)
                .build();
    }


    @Transactional(readOnly = true)
    public MatchRecord findById(Long recordId) {
        return matchRecordRepository.findById(recordId)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_RECORD));
    }

    @Transactional
    public void deleteRecord(Long recordId) {
        MatchRecord record = findById(recordId);
        matchRecordRepository.delete(record);
    }


}
