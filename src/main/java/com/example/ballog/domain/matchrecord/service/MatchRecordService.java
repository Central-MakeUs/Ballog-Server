package com.example.ballog.domain.matchrecord.service;

import com.example.ballog.domain.login.entity.User;
import com.example.ballog.domain.match.entity.Matches;
import com.example.ballog.domain.match.repository.MatchesRepository;
import com.example.ballog.domain.match.service.MatchesService;
import com.example.ballog.domain.matchrecord.dto.request.MatchRecordRequest;
import com.example.ballog.domain.matchrecord.dto.response.MatchRecordResponse;
import com.example.ballog.domain.matchrecord.entity.MatchRecord;
import com.example.ballog.domain.matchrecord.repository.MatchRecordRepository;
import com.example.ballog.global.common.exception.CustomException;
import com.example.ballog.global.common.exception.enums.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MatchRecordService {
    private final MatchesRepository matchesRepository;
    private final MatchRecordRepository matchRecordRepository;

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
                .build();
    }



}
