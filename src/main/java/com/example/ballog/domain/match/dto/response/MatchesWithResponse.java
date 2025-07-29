package com.example.ballog.domain.match.dto.response;

import com.example.ballog.domain.match.entity.Matches;

public class MatchesWithResponse {
    private final Matches matches;
    private final MatchesResponse response;

    public MatchesWithResponse(Matches matches, MatchesResponse response) {
        this.matches = matches;
        this.response = response;
    }

    public Matches getMatches() {
        return matches;
    }

    public MatchesResponse getResponse() {
        return response;
    }
}
