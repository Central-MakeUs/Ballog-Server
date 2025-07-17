package com.example.ballog.domain.match.service;

import com.example.ballog.domain.login.entity.BaseballTeam;
import com.example.ballog.domain.match.dto.request.MatchesRequest;
import com.example.ballog.domain.match.dto.response.MatchesGroupedResponse;
import com.example.ballog.domain.match.dto.response.MatchesResponse;
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

    public MatchesResponse createMatches(MatchesRequest request){
        Matches match = new Matches();
        match.setMatchesDate(request.getMatchesDate());
        match.setMatchesTime(request.getMatchesTime());
        match.setHomeTeam(request.getHomeTeam());
        match.setAwayTeam(request.getAwayTeam());
        match.setStadium(request.getStadium());
        match.setMatchesResult(request.getMatchesResult());

        Matches saved = matchesRepository.save(match);
        return new MatchesResponse(
                saved.getMatchesId(),
                saved.getMatchesDate(),
                saved.getMatchesTime(),
                saved.getHomeTeam(),
                saved.getAwayTeam(),
                saved.getStadium(),
                saved.getMatchesResult()
        );
    }


    public List<MatchesResponse> getTodayMatches() {
        LocalDate today = LocalDate.now();
        List<Matches> matchesList = matchesRepository.findAllByMatchesDate(today);
        return matchesList.stream()
                .map(MatchesResponse::from)
                .collect(Collectors.toList());
    }

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
        Matches match = matchesRepository.findById(matchId)
                .orElseThrow(() -> new CustomException(ErrorCode.MATCH_NOT_FOUND));
        return MatchesResponse.from(match);
    }

    @Transactional
    public MatchesResponse updateMatch(Long matchId, MatchesRequest request) {
        Matches match = matchesRepository.findById(matchId)
                .orElseThrow(() -> new CustomException(ErrorCode.MATCH_NOT_FOUND));

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

        boolean resultChanged = false;
        if (request.getMatchesResult() != null &&
                !Objects.equals(match.getMatchesResult(), request.getMatchesResult())) {
            resultChanged = true;
            match.setMatchesResult(request.getMatchesResult());
        }

        if (resultChanged) {
            updateMatchRecordsResult(match);
        }

        return MatchesResponse.from(matchesRepository.save(match));
    }

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


    public void deleteMatch(Long matchId) {
        Matches match = matchesRepository.findById(matchId)
                .orElseThrow(() -> new CustomException(ErrorCode.MATCH_NOT_FOUND));

        matchesRepository.delete(match);
    }

}
