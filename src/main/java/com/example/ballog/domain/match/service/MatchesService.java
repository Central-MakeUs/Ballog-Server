package com.example.ballog.domain.match.service;

import com.example.ballog.domain.match.dto.request.MatchesRequest;
import com.example.ballog.domain.match.dto.response.MatchesGroupedResponse;
import com.example.ballog.domain.match.dto.response.MatchesResponse;
import com.example.ballog.domain.match.entity.Matches;
import com.example.ballog.domain.match.repository.MatchesRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

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


}
