package com.example.ballog.domain.match.service;

import com.example.ballog.domain.match.dto.request.MatchesRequest;
import com.example.ballog.domain.match.dto.response.MatchesResponse;
import com.example.ballog.domain.match.entity.Matches;
import com.example.ballog.domain.match.repository.MatchesRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MatchesService {

    private final MatchesRepository matchesRepository;

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
}
