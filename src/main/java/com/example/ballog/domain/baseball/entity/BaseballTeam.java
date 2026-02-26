package com.example.ballog.domain.baseball.entity;

import java.time.LocalDateTime;
import java.util.*;

public enum BaseballTeam {
    DOOSAN_BEARS("두산 베어스"), 
    LOTTE_GIANTS("롯데 자이언츠"),
    SAMSUNG_LIONS("삼성 라이온즈"),
    KIWOOM_HEROES("키움 히어로즈"),
    HANWHA_EAGLES("한화 이글스"),
    KIA_TIGERS("KIA 타이거즈"),
    KT_WIZ("KT 위즈"),
    LG_TWINS("LG 트윈스"),
    NC_DINOS("NC 다이노스"),
    SSG_LANDERS("SSG 랜더스"),
    NONE("응원팀 없음");

    private final String displayName;

    BaseballTeam(String displayName) {
        this.displayName = displayName;
    }

    // Enum 자체는 불변 → 순위 Map에서 관리
    private static final Map<BaseballTeam, TeamRank> teamRankMap = new HashMap<>();

    static {
        // 초기값 설정
        teamRankMap.put(LG_TWINS, new TeamRank(1, LocalDateTime.now()));
        teamRankMap.put(HANWHA_EAGLES, new TeamRank(2, LocalDateTime.now()));
        teamRankMap.put(SSG_LANDERS, new TeamRank(3, LocalDateTime.now()));
        teamRankMap.put(SAMSUNG_LIONS, new TeamRank(4, LocalDateTime.now()));
        teamRankMap.put(NC_DINOS, new TeamRank(5, LocalDateTime.now()));
        teamRankMap.put(KT_WIZ, new TeamRank(6, LocalDateTime.now()));
        teamRankMap.put(LOTTE_GIANTS, new TeamRank(7, LocalDateTime.now()));
        teamRankMap.put(DOOSAN_BEARS, new TeamRank(8, LocalDateTime.now()));
        teamRankMap.put(KIA_TIGERS, new TeamRank(9, LocalDateTime.now()));
        teamRankMap.put(KIWOOM_HEROES, new TeamRank(10, LocalDateTime.now()));
    }

    // 순위 조회
    public static TeamRank getTeamRank(BaseballTeam team) {
        return teamRankMap.get(team);
    }

    // 순위 업데이트
    public static void updateTeamRank(BaseballTeam team, int newRank) {
        teamRankMap.compute(team, (k, v) -> {
            if (v == null) return new TeamRank(newRank, LocalDateTime.now());
            v.setRank(newRank);
            return v;
        });
    }

    // 전체 순위 조회 (정렬 가능)
    public static List<Map.Entry<BaseballTeam, TeamRank>> getRankedTeams() {
        List<Map.Entry<BaseballTeam, TeamRank>> list = new ArrayList<>(teamRankMap.entrySet());
        list.sort(Comparator.comparingInt(e -> e.getValue().getRank()));
        return list;
    }
    
}
