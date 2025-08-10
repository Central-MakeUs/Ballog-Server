package com.example.ballog.domain.matchrecord.service;

import com.example.ballog.domain.Image.entity.Image;
import com.example.ballog.domain.Image.respository.ImageRepository;
import com.example.ballog.domain.Image.service.S3Service;
import com.example.ballog.domain.emotion.entity.Emotion;
import com.example.ballog.domain.emotion.entity.EmotionType;
import com.example.ballog.domain.emotion.repository.EmotionRepository;
import com.example.ballog.domain.login.entity.User;
import com.example.ballog.domain.match.entity.Matches;
import com.example.ballog.domain.match.repository.MatchesRepository;
import com.example.ballog.domain.matchrecord.dto.request.MatchRecordRequest;
import com.example.ballog.domain.matchrecord.dto.response.*;
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
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MatchRecordService {

    @Value("${app.default-image-url}")
    private String defaultImageUrl;
    private final MatchesRepository matchesRepository;
    private final MatchRecordRepository matchRecordRepository;
    private final EmotionRepository emotionRepository;
    private final ImageRepository imageRepository;
    private final S3Service s3Service;


    @Transactional
    public MatchRecordResponse createRecord(MatchRecordRequest request, User user) {
        Matches matches = matchesRepository.findById(request.getMatchesId())
                .orElseThrow(() -> new CustomException(ErrorCode.MATCH_NOT_FOUND));


        if (matchRecordRepository.existsByUserAndMatches(user, matches)) {
            throw new CustomException(ErrorCode.ALREADY_RECORDED);
        }

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
                .homeTeam(matches.getHomeTeam())
                .awayTeam(matches.getAwayTeam())
                .matchDate(matches.getMatchesDate())
                .matchTime(matches.getMatchesTime())
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


    @Transactional(readOnly = true)
    public MatchRecordDetailResponse getRecordDetail(Long recordId, User currentUser) {
        MatchRecord record = matchRecordRepository.findById(recordId)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_RECORD));

        if (!record.getUser().getUserId().equals(currentUser.getUserId())) {
            throw new CustomException(ErrorCode.RECORD_NOT_OWNED);
        }

        Matches match = record.getMatches();

        List<ImageInfo> imageList = imageRepository.findByMatchRecord(record).stream()
                .map(img -> ImageInfo.builder()
                        .imageUrl(img.getImageUrl())
                        .createdAt(img.getCreatedAt())
                        .build())
                .collect(Collectors.toList());


        List<Emotion> emotions = emotionRepository.findByMatchRecordId(recordId);
        emotions.sort(Comparator.comparing(Emotion::getCreatedAt));

        long totalCount = emotions.size();
        long positiveCount = emotions.stream()
                .filter(e -> e.getEmotionType() == EmotionType.POSITIVE)
                .count();
        long negativeCount = emotions.stream()
                .filter(e -> e.getEmotionType() == EmotionType.NEGATIVE)
                .count();

        double positivePercent = totalCount == 0 ? 0.0 : (positiveCount * 100.0) / totalCount;
        double negativePercent = totalCount == 0 ? 0.0 : (negativeCount * 100.0) / totalCount;

        //1분 단위 감정 그룹핑 (동일 감정 3회 이상)
        List<EmotionGroupInfo> emotionGroupList = new ArrayList<>();

        EmotionType currentType = null;
        LocalDateTime currentGroupStart = null;
        long currentCount = 0;

        for (Emotion e : emotions) {
            LocalDateTime truncatedToMinute = e.getCreatedAt().truncatedTo(ChronoUnit.MINUTES);

            if (currentType == null) {
                currentType = e.getEmotionType();
                currentGroupStart = truncatedToMinute;
                currentCount = 1;
            } else {
                if (truncatedToMinute.equals(currentGroupStart) && e.getEmotionType() == currentType) {
                    currentCount++;
                } else {
                    if (currentCount >= 3) {
                        emotionGroupList.add(new EmotionGroupInfo(currentGroupStart, currentType, currentCount));
                    }
                    currentType = e.getEmotionType();
                    currentGroupStart = truncatedToMinute;
                    currentCount = 1;
                }
            }
        }

        if (currentCount >= 3) {
            emotionGroupList.add(new EmotionGroupInfo(currentGroupStart, currentType, currentCount));
        }


        return MatchRecordDetailResponse.builder()
                .matchRecordId(record.getMatchrecordId())
                .matchesId(match.getMatchesId())
                .stadium(match.getStadium())
                .homeTeam(match.getHomeTeam())
                .awayTeam(match.getAwayTeam())
                .matchDate(match.getMatchesDate())
                .matchTime(match.getMatchesTime())
                .userId(record.getUser().getUserId())
                .watchCnt(record.getWatchCnt())
                .result(record.getResult())
                .baseballTeam(record.getBaseballTeam())
                .positiveEmotionPercent(positivePercent)
                .negativeEmotionPercent(negativePercent)
                .imageList(imageList)
                .emotionGroupList(emotionGroupList)
                .build();
    }

    public MatchRecordDetailResponse getRecordDetailByMatchId(Long matchId, User currentUser) {
        MatchRecord record = matchRecordRepository.findByMatches_MatchesIdAndUser_UserId(matchId, currentUser.getUserId())
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_RECORD));

        return getRecordDetail(record.getMatchrecordId(), currentUser);
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
                    .stadium(match.getStadium())
                    .homeTeam(match.getHomeTeam())
                    .awayTeam(match.getAwayTeam())
                    .matchDate(match.getMatchesDate())
                    .matchTime(match.getMatchesTime())
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

        List<Image> images = imageRepository.findAllByMatchRecord(record);

        for (Image image : images) {
            s3Service.deleteFileFromS3(image.getImageUrl());
        }

        emotionRepository.deleteAllByMatchRecord(record);
        imageRepository.deleteAllByMatchRecord(record);
        matchRecordRepository.delete(record);
    }

}
