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
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
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

        EmotionStats stats = calculateEmotionStats(recordId);
        double positive = stats.positivePercent();
        double negative = stats.negativePercent();

        Map<EmotionType, Long> countMap;
        countMap = emotionRepository.countByEmotionType(recordId).stream()
                .collect(Collectors.toMap(
                        row -> (EmotionType) row[0],
                        row -> (Long) row[1]
                ));

        long positiveCnt = countMap.getOrDefault(EmotionType.POSITIVE, 0L);
        long negativeCnt = countMap.getOrDefault(EmotionType.NEGATIVE, 0L);


        List<ImageInfo> imageList = getImageInfos(record);
        List<EmotionGroupInfo> emotionGroups = groupEmotions(recordId);

        return MatchRecordDetailResponse.from(record, positive, negative, positiveCnt, negativeCnt, imageList, emotionGroups);
    }


    public MatchRecordDetailResponse getRecordDetailByMatchId(Long matchId, User currentUser) {
        MatchRecord record = matchRecordRepository
                .findByMatches_MatchesIdAndUser_UserId(matchId, currentUser.getUserId())
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_RECORD));

        return getRecordDetail(record.getMatchrecordId(), currentUser);
    }



    @Transactional(readOnly = true)
    public MatchTeamEmotionResponse getTeamEmotionStatsByMatch(Long matchId, User user) {

        Matches match = matchesRepository.findById(matchId).orElseThrow(() -> new CustomException(ErrorCode.MATCH_NOT_FOUND));
        BaseballTeam userTeam = user.getBaseballTeam();

        // NONE 제외 + 홈/원정 팀만 긍정/부정 집계
        List<Object[]> rows =
                emotionRepository.countEmotionByMatchExcludeNone(
                        matchId,
                        match.getHomeTeam(),
                        match.getAwayTeam(),
                        BaseballTeam.NONE
                );

        // team -> emotionType -> count
        Map<BaseballTeam, Map<EmotionType, Long>> statMap = new EnumMap<>(BaseballTeam.class);

        for (Object[] row : rows) {
            BaseballTeam team = (BaseballTeam) row[0];
            EmotionType type = (EmotionType) row[1];
            Long count = (Long) row[2];

            statMap
                    .computeIfAbsent(team, t -> new EnumMap<>(EmotionType.class))
                    .put(type, count);

        }

        //1. 응원팀 있는 경우 → 해당 팀의 긍/부정 "개수"
        if (userTeam != null && userTeam != BaseballTeam.NONE) {

            Map<EmotionType, Long> userStat = statMap.getOrDefault(userTeam, Map.of());

            long positiveCnt = userStat.getOrDefault(EmotionType.POSITIVE, 0L);
            long negativeCnt = userStat.getOrDefault(EmotionType.NEGATIVE, 0L);

            return MatchTeamEmotionResponse.forUserTeam(
                    match,
                    userTeam,
                    positiveCnt,
                    negativeCnt
            );
        }

        // 2. 응원팀 NONE → 홈/원정 팀별 퍼센트
        double[] homePercent = calculatePercent(statMap.get(match.getHomeTeam()));
        double[] awayPercent = calculatePercent(statMap.get(match.getAwayTeam()));

        return MatchTeamEmotionResponse.forNoTeam(
                match,
                homePercent[0], homePercent[1],
                awayPercent[0], awayPercent[1]
        );
    }

    private double[] calculatePercent(Map<EmotionType, Long> stat) {

        if (stat == null || stat.isEmpty()) {
            return new double[]{0.0, 0.0};
        }

        long positive = stat.getOrDefault(EmotionType.POSITIVE, 0L);
        long negative = stat.getOrDefault(EmotionType.NEGATIVE, 0L);
        long total = positive + negative;

        if (total == 0) {
            return new double[]{0.0, 0.0};
        }

        return new double[]{
                positive * 100.0 / total,
                negative * 100.0 / total
        };
    }
    
    @Transactional(readOnly = true)
    public MatchRecordListResponse getAllRecordsByUser(User user) {
        List<MatchRecord> records = matchRecordRepository.findAllByUserOrderByMatchrecordIdDesc(user);
        WinStats winStats = calculateWinStats(records);
        EmotionStats emotionStats = calculateEmotionStatsForUser(user);

        List<MatchRecordSummaryResponse> recordResponses = records.stream()
                .map(this::toSummaryResponse)
                .toList();

        return MatchRecordListResponse.from( records.size(), winStats.winRate(), emotionStats.positivePercent(), emotionStats.negativePercent(), recordResponses);
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

        return matchRecordRepository.save(new MatchRecord(null, match, user, count + 1, result, user.getBaseballTeam(), defaultImageUrl)
        );
    }

    private MatchRecordResponse toMatchRecordResponse(MatchRecord record, Matches match) {
        return MatchRecordResponse.from(record, match);
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

    private MatchRecordSummaryResponse toSummaryResponse(MatchRecord record) {
        Matches match = record.getMatches();

        return MatchRecordSummaryResponse.from(record);

    }

    private void deleteAssociatedData(MatchRecord record) {//FK로 연관되어 있는 것들 먼저 삭제
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

    private WinStats calculateWinStats(List<MatchRecord> records) {
        long winCount = records.stream().filter(r -> r.getResult() == Result.WIN).count();
        double winRate = records.isEmpty() ? 0.0 : (winCount * 100.0) / records.size();
        return new WinStats(winRate);
    }
    private record WinStats(double winRate) {}


}