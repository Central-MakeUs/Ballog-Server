package com.example.ballog.domain.match.service;

import com.example.ballog.domain.alert.service.MatchAlertSetupService;
import com.example.ballog.domain.login.entity.BaseballTeam;
import com.example.ballog.domain.match.dto.request.MatchesRequest;
import com.example.ballog.domain.match.dto.response.MatchesGroupedResponse;
import com.example.ballog.domain.match.dto.response.MatchesResponse;
import com.example.ballog.domain.match.dto.response.MatchesWithResponse;
import com.example.ballog.domain.match.entity.Matches;
import com.example.ballog.domain.match.repository.MatchesRepository;
import com.example.ballog.domain.matchrecord.entity.MatchRecord;
import com.example.ballog.domain.matchrecord.entity.Result;
import com.example.ballog.domain.matchrecord.repository.MatchRecordRepository;
import com.example.ballog.global.common.exception.CustomException;
import com.example.ballog.global.common.exception.enums.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MatchesService {

    private final MatchesRepository matchesRepository;
    private final MatchRecordRepository matchRecordRepository;
    private final MatchAlertSetupService matchAlertSetupService;

    public MatchesWithResponse createMatches(MatchesRequest request) {
        Matches match = buildMatchFromRequest(request);
        Matches saved = matchesRepository.save(match);
        MatchesResponse response = MatchesResponse.from(saved);

        matchAlertSetupService.scheduleUserAlertsForMatch(saved);   //경기 저장 시점에 푸시 알림 스케줄링

        return new MatchesWithResponse(saved, response);
    }


    public List<MatchesResponse> getTodayMatches() {
        LocalDate today = LocalDate.now();
        List<Matches> matchesList = matchesRepository.findAllByMatchesDate(today);
        return matchesList.stream()
                .map(MatchesResponse::from)
                .collect(Collectors.toList());
    }

    /**
     * 모든 경기 일정을 날짜별로 그룹화하여 조회
     */
    public Map<String, List<MatchesGroupedResponse>> getAllMatchesGroupedByDate() {
        List<Matches> matchesList = matchesRepository.findAll();
        return matchesList.stream()
                .collect(Collectors.groupingBy(
                        match -> match.getMatchesDate().toString(),
                        TreeMap::new,
                        Collectors.mapping(MatchesGroupedResponse::from, Collectors.toList())
                ));
    }

    public MatchesResponse getMatchDetail(Long matchId) {
        Matches match = findMatchById(matchId);
        return MatchesResponse.from(match);
    }

    @Transactional
    public MatchesResponse updateMatch(Long matchId, MatchesRequest request) {
        Matches match = findMatchById(matchId);

        updateMatchFields(match, request);

        boolean resultChanged = isMatchResultChanged(match, request);

        if (resultChanged) {
            updateMatchRecordsResult(match);
        }

        return MatchesResponse.from(matchesRepository.save(match));
    }

    public void deleteMatch(Long matchId) {
        Matches match = findMatchById(matchId);
        matchesRepository.delete(match);
    }

    private Matches findMatchById(Long matchId) {
        return matchesRepository.findById(matchId)
                .orElseThrow(() -> new CustomException(ErrorCode.MATCH_NOT_FOUND));
    }

    private Matches buildMatchFromRequest(MatchesRequest request) {
        Matches match = new Matches();
        match.setMatchesDate(request.getMatchesDate());
        match.setMatchesTime(request.getMatchesTime());
        match.setHomeTeam(request.getHomeTeam());
        match.setAwayTeam(request.getAwayTeam());
        match.setStadium(request.getStadium());
        match.setMatchesResult(request.getMatchesResult());
        return match;
    }

    private void updateMatchFields(Matches match, MatchesRequest request) {
        if (request.getMatchesDate() != null) {
            match.setMatchesDate(request.getMatchesDate());
        }
        if (request.getMatchesTime() != null) {
            match.setMatchesTime(request.getMatchesTime());
        }
        if (request.getHomeTeam() != null) {
            match.setHomeTeam(request.getHomeTeam());
        }
        if (request.getAwayTeam() != null) {
            match.setAwayTeam(request.getAwayTeam());
        }
        if (request.getStadium() != null) {
            match.setStadium(request.getStadium());
        }
        if (request.getMatchesResult() != null) {
            match.setMatchesResult(request.getMatchesResult());
        }
    }

    private boolean isMatchResultChanged(Matches match, MatchesRequest request) {
        return request.getMatchesResult() != null &&
                !Objects.equals(match.getMatchesResult(), request.getMatchesResult());
    }

    /**
     * 매치 결과 변경 시 유저가 작성한 해당 경기 기록 결과 자동 업데이트
     */
    private void updateMatchRecordsResult(Matches match) {
        List<MatchRecord> records = matchRecordRepository.findAllByMatches_MatchesId(match.getMatchesId());

        for (MatchRecord record : records) {
            Result result = determineResult(
                    match.getMatchesResult(),
                    match.getHomeTeam(),
                    match.getAwayTeam(),
                    record.getBaseballTeam()
            );
            record.setResult(result);
        }
        matchRecordRepository.saveAll(records);
    }

    /**
     * 경기 결과 문자열을 분석해 해당 팀의 결과(WIN, LOSS, DRAW) 판단
     */
    private Result determineResult(String matchResult, BaseballTeam homeTeam, BaseballTeam awayTeam, BaseballTeam userTeam) {
        String[] scores = matchResult.split(":");
        if (scores.length != 2) {
            throw new CustomException(ErrorCode.MATCH_RESULT_FORMAT_INVALID);
        }

        int homeScore = Integer.parseInt(scores[0].trim());
        int awayScore = Integer.parseInt(scores[1].trim());

        if (homeScore == awayScore) {
            return Result.DRAW;
        }

        boolean userIsHome = userTeam == homeTeam;
        boolean userWon = (userIsHome && homeScore > awayScore) || (!userIsHome && awayScore > homeScore);

        return userWon ? Result.WIN : Result.LOSS;
    }

}
