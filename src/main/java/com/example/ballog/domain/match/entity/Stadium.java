package com.example.ballog.domain.match.entity;

public enum Stadium {
    JAMSIL("잠실종합운동장", "두산 베어스, LG 트윈스"),

    SAJIK("사직야구장", "롯데 자이언츠"),

    DAEGU("대구삼성라이온즈파크", "삼성 라이온즈"),

    GOCHUK("고척스카이돔", "키움 히어로즈"),

    DAEJEON("대전한화생명이글스파크", "한화 이글스"),

    GWANGJU("광주기아챔피언스필드", "KIA 타이거즈"),

    SUWON("수원KT위즈파크", "KT 위즈"),

    CHANGWON("창원NC파크", "NC 다이노스"),

    INCHEON("인천SSG랜더스필드", "SSG 랜더스"),

    NONE("미지정 경기장", "없음");

    private final String stadiumName;
    private final String homeTeams;

    Stadium(String stadiumName, String homeTeams) {
        this.stadiumName = stadiumName;
        this.homeTeams = homeTeams;
    }

}
