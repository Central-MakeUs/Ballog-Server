package com.example.ballog.domain.matchrecord.service;

import com.example.ballog.domain.Image.entity.Image;
import com.example.ballog.domain.Image.respository.ImageRepository;
import com.example.ballog.domain.Image.service.S3Service;
import com.example.ballog.domain.emotion.entity.Emotion;
import com.example.ballog.domain.emotion.entity.EmotionType;
import com.example.ballog.domain.emotion.repository.EmotionRepository;
import com.example.ballog.domain.login.entity.BaseballTeam;
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
        Matches match = findMatch(request.getMatchesId());

        validateNotAlreadyRecorded(user, match);

        MatchRecord record = saveMatchRecord(user, match, request.getResult());

        return toMatchRecordResponse(record, match);
    }

    @Transactional(readOnly = true)
    public MatchRecordDetailResponse getRecordDetail(Long recordId, User currentUser) {
        MatchRecord record = findRecordOwnedByUser(recordId, currentUser);

        List<ImageInfo> imageList = getImageInfos(record);
        EmotionStats stats = calculateEmotionStats(recordId);
        List<EmotionGroupInfo> emotionGroups = groupEmotions(recordId);

        Matches match = record.getMatches();
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
                .positiveEmotionPercent(stats.positivePercent)
                .negativeEmotionPercent(stats.negativePercent)
                .imageList(imageList)
                .emotionGroupList(emotionGroups)
                .build();
    }

    public MatchRecordDetailResponse getRecordDetailByMatchId(Long matchId, User currentUser) {
        MatchRecord record = matchRecordRepository
                .findByMatches_MatchesIdAndUser_UserId(matchId, currentUser.getUserId())
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_RECORD));

        return getRecordDetail(record.getMatchrecordId(), currentUser);
    }

    @Transactional(readOnly = true)
    public MatchTeamEmotionResponse getTeamEmotionStatsByMatch(Long matchId, User currentUser) {
        Matches match = matchesRepository.findById(matchId)
                .orElseThrow(() -> new CustomException(ErrorCode.MATCH_NOT_FOUND));

        BaseballTeam userTeam = currentUser.getBaseballTeam();

        //응원팀이 설정된 경우 (NONE이 아님)
        if (userTeam != null && userTeam != BaseballTeam.NONE) {
            EmotionStats teamStats = calculateEmotionStatsByTeam(userTeam);
            List<EmotionGroupInfo> teamEmotionGroups = groupEmotionsByTeam(userTeam);

            return MatchTeamEmotionResponse.builder()
                    .matchId(match.getMatchesId())
                    .stadium(match.getStadium().name())
                    .homeTeam(match.getHomeTeam().name())
                    .awayTeam(match.getAwayTeam().name())
                    .matchDate(match.getMatchesDate())
                    .matchTime(match.getMatchesTime())
                    .userTeam(userTeam.name())
                    .positiveEmotionPercent(teamStats.positivePercent)
                    .negativeEmotionPercent(teamStats.negativePercent)
                    .emotionGroupList(teamEmotionGroups)
                    .build();
        }

        //응원팀이 NONE인 경우 → 경기의 홈팀, 어웨이팀별 통계 반환
        EmotionStats homeStats = calculateEmotionStatsByTeam(match.getHomeTeam());
        EmotionStats awayStats = calculateEmotionStatsByTeam(match.getAwayTeam());

        return MatchTeamEmotionResponse.builder()
                .matchId(match.getMatchesId())
                .stadium(match.getStadium().name())
                .homeTeam(match.getHomeTeam().name())
                .awayTeam(match.getAwayTeam().name())
                .matchDate(match.getMatchesDate())
                .matchTime(match.getMatchesTime())
                .userTeam("NONE")
                .homeTeamPositivePercent(homeStats.positivePercent)
                .homeTeamNegativePercent(homeStats.negativePercent)
                .awayTeamPositivePercent(awayStats.positivePercent)
                .awayTeamNegativePercent(awayStats.negativePercent)
                .build();
    }


    @Transactional(readOnly = true)
    public MatchRecordListResponse getAllRecordsByUser(User user) {
        List<MatchRecord> records = matchRecordRepository.findAllByUserOrderByMatchrecordIdDesc(user);
        WinStats winStats = calculateWinStats(records);
        EmotionStats emotionStats = calculateEmotionStatsForUser(user);

        List<MatchRecordSummaryResponse> recordResponses = records.stream()
                .map(this::toSummaryResponse)
                .toList();

        return MatchRecordListResponse.builder()
                .totalCount(records.size())
                .winRate(winStats.winRate)
                .totalPositiveEmotionPercent(emotionStats.positivePercent)
                .totalNegativeEmotionPercent(emotionStats.negativePercent)
                .records(recordResponses)
                .build();
    }

    @Transactional
    public void deleteRecord(Long recordId, User user) {
        MatchRecord record = findById(recordId);

        if (!record.getUser().getUserId().equals(user.getUserId())) {
            throw new CustomException(ErrorCode.RECORD_NOT_OWNED_DELETE);
        }

        deleteAssociatedData(record);
        matchRecordRepository.delete(record);
    }

    private Matches findMatch(Long matchId) {
        return matchesRepository.findById(matchId)
                .orElseThrow(() -> new CustomException(ErrorCode.MATCH_NOT_FOUND));
    }

    private void validateNotAlreadyRecorded(User user, Matches match) {
        if (matchRecordRepository.existsByUserAndMatches(user, match)) {
            throw new CustomException(ErrorCode.ALREADY_RECORDED);
        }
    }

    private MatchRecord saveMatchRecord(User user, Matches match, Result result) {
        long count = matchRecordRepository.countByUser(user);
        MatchRecord record = new MatchRecord();
        record.setMatches(match);
        record.setUser(user);
        record.setWatchCnt(count + 1);
        record.setResult(result);
        record.setBaseballTeam(user.getBaseballTeam());
        record.setDefaultImageUrl(defaultImageUrl);
        return matchRecordRepository.save(record);
    }

    private MatchRecordResponse toMatchRecordResponse(MatchRecord record, Matches match) {
        return MatchRecordResponse.builder()
                .matchRecordId(record.getMatchrecordId())
                .matchesId(match.getMatchesId())
                .homeTeam(match.getHomeTeam())
                .awayTeam(match.getAwayTeam())
                .matchDate(match.getMatchesDate())
                .matchTime(match.getMatchesTime())
                .userId(record.getUser().getUserId())
                .watchCnt(record.getWatchCnt())
                .result(record.getResult())
                .baseballTeam(record.getBaseballTeam())
                .defaultImageUrl(record.getDefaultImageUrl())
                .build();
    }

    private MatchRecord findRecordOwnedByUser(Long recordId, User currentUser) {
        MatchRecord record = findById(recordId);
        if (!record.getUser().getUserId().equals(currentUser.getUserId())) {
            throw new CustomException(ErrorCode.RECORD_NOT_OWNED);
        }
        return record;
    }

    private MatchRecord findById(Long recordId) {
        return matchRecordRepository.findById(recordId)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_RECORD));
    }

    private List<ImageInfo> getImageInfos(MatchRecord record) {
        return imageRepository.findByMatchRecord(record).stream()
                .map(img -> ImageInfo.builder()
                        .imageUrl(img.getImageUrl())
                        .createdAt(img.getCreatedAt())
                        .build())
                .toList();
    }

    private EmotionStats calculateEmotionStats(Long recordId) {
        List<Emotion> emotions = emotionRepository.findByMatchRecordId(recordId);
        return EmotionStats.from(emotions);
    }

    private EmotionStats calculateEmotionStatsForUser(User user) {
        List<Emotion> allEmotions = emotionRepository.findByUserId(user.getUserId());
        return EmotionStats.from(allEmotions);
    }

    private List<EmotionGroupInfo> groupEmotions(Long recordId) {
        List<Emotion> emotions = emotionRepository.findByMatchRecordId(recordId);
        emotions.sort(Comparator.comparing(Emotion::getCreatedAt));

        List<EmotionGroupInfo> groups = new ArrayList<>();
        EmotionType currentType = null;
        LocalDateTime groupStart = null;
        long count = 0;

        for (Emotion e : emotions) {
            LocalDateTime minute = e.getCreatedAt().truncatedTo(ChronoUnit.MINUTES);
            if (currentType == null || !minute.equals(groupStart) || e.getEmotionType() != currentType) {
                if (count >= 3) {
                    groups.add(new EmotionGroupInfo(groupStart, currentType, count));
                }
                currentType = e.getEmotionType();
                groupStart = minute;
                count = 1;
            } else {
                count++;
            }
        }
        if (count >= 3) {
            groups.add(new EmotionGroupInfo(groupStart, currentType, count));
        }
        return groups;
    }

    private WinStats calculateWinStats(List<MatchRecord> records) {
        long winCount = records.stream().filter(r -> r.getResult() == Result.WIN).count();
        double winRate = records.isEmpty() ? 0.0 : (winCount * 100.0) / records.size();
        return new WinStats(winRate);
    }

    private MatchRecordSummaryResponse toSummaryResponse(MatchRecord record) {
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
    }

    private void deleteAssociatedData(MatchRecord record) {
        List<Image> images = imageRepository.findAllByMatchRecord(record);
        images.forEach(img -> s3Service.deleteFileFromS3(img.getImageUrl()));
        emotionRepository.deleteAllByMatchRecord(record);
        imageRepository.deleteAllByMatchRecord(record);
    }

    private record EmotionStats(double positivePercent, double negativePercent) {
        static EmotionStats from(List<Emotion> emotions) {
            long total = emotions.size();
            long positive = emotions.stream().filter(e -> e.getEmotionType() == EmotionType.POSITIVE).count();
            long negative = emotions.stream().filter(e -> e.getEmotionType() == EmotionType.NEGATIVE).count();
            return new EmotionStats(
                    total == 0 ? 0.0 : (positive * 100.0) / total,
                    total == 0 ? 0.0 : (negative * 100.0) / total
            );
        }
    }

    private EmotionStats calculateEmotionStatsByTeam(BaseballTeam baseballTeam) {
        List<Emotion> teamEmotions = emotionRepository.findByUserBaseballTeam(baseballTeam);
        return EmotionStats.from(teamEmotions);
    }

    private List<EmotionGroupInfo> groupEmotionsByTeam(BaseballTeam baseballTeam) {
        List<Emotion> emotions = emotionRepository.findByUserBaseballTeam(baseballTeam);
        emotions.sort(Comparator.comparing(Emotion::getCreatedAt));

        List<EmotionGroupInfo> groups = new ArrayList<>();
        EmotionType currentType = null;
        LocalDateTime groupStart = null;
        long count = 0;

        for (Emotion e : emotions) {
            LocalDateTime minute = e.getCreatedAt().truncatedTo(ChronoUnit.MINUTES);
            if (currentType == null || !minute.equals(groupStart) || e.getEmotionType() != currentType) {
                if (count >= 3) {
                    groups.add(new EmotionGroupInfo(groupStart, currentType, count));
                }
                currentType = e.getEmotionType();
                groupStart = minute;
                count = 1;
            } else {
                count++;
            }
        }

        if (count >= 3) {
            groups.add(new EmotionGroupInfo(groupStart, currentType, count));
        }

        return groups;
    }



    private record WinStats(double winRate) {}
}