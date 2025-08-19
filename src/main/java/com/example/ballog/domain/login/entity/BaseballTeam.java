package com.example.ballog.domain.login.entity;

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
    
}
