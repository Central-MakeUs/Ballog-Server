package com.example.ballog.domain.emotion.repository;

import com.example.ballog.domain.emotion.dto.response.TeamEmotionResponse;
import com.example.ballog.domain.emotion.entity.Emotion;
import com.example.ballog.domain.baseball.entity.BaseballTeam;
import com.example.ballog.domain.matchrecord.entity.MatchRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EmotionRepository extends JpaRepository<Emotion, Long> {
    List<Emotion> findByMatchRecord(MatchRecord matchRecord);
    @Query("SELECT e FROM Emotion e WHERE e.matchRecord.matchrecordId = :recordId")
    List<Emotion> findByMatchRecordId(@Param("recordId") Long recordId);

    @Query("""
    select e.emotionType, count(e)
    from Emotion e
    where e.matchRecord.matchrecordId = :recordId
    group by e.emotionType
    """)
    List<Object[]> countByEmotionType(@Param("recordId") Long recordId); //감정관련 쿼리 한번에 불러오기

    @Query("""
    select u.baseballTeam, e.emotionType, count(e)
    from Emotion e
    join User u on u.userId = e.userId
    where e.matches.matchesId = :matchId
      and u.baseballTeam != :noneTeam
      and u.baseballTeam in (:homeTeam, :awayTeam)
    group by u.baseballTeam, e.emotionType
    """)
    List<Object[]> countEmotionByMatchExcludeNone(
            @Param("matchId") Long matchId,
            @Param("homeTeam") BaseballTeam homeTeam,
            @Param("awayTeam") BaseballTeam awayTeam,
            @Param("noneTeam") BaseballTeam noneTeam
    );


    @Query("""
    select u.baseballTeam, e.emotionType, count(e)
    from Emotion e
    join User
              u on u.userId = e.userId
    where e.matches.matchesId = :matchId
    group by u.baseballTeam, e.emotionType
    """)
    List<Object[]> countEmotionByMatch(@Param("matchId") Long matchId); //내가 응원하는 팀의 전체 클릭 수 (긍정/부정)

    List<Emotion> findByUserId(Long userId);

    @Modifying
    @Query("delete from Emotion e where e.matchRecord.user.userId = :userId")
    void deleteAllByUserUserId(@Param("userId") Long userId);

    void deleteAllByMatchRecord(MatchRecord matchRecord);
    @Query("""
                SELECT e
                FROM Emotion e
                JOIN User u ON e.userId = u.userId
                WHERE u.baseballTeam = :baseballTeam
            """)
    List<Emotion> findByUserBaseballTeam(@Param("baseballTeam") BaseballTeam baseballTeam);

    interface EmotionCountProjection {
        Long getUserId();
        Long getPositiveCnt();
        Long getNegativeCnt();
    }

    @Query("""
    SELECT e.userId as userId,
           SUM(CASE WHEN e.emotionType = com.example.ballog.domain.emotion.entity.EmotionType.POSITIVE THEN 1 ELSE 0 END) as positiveCnt,
           SUM(CASE WHEN e.emotionType = com.example.ballog.domain.emotion.entity.EmotionType.NEGATIVE THEN 1 ELSE 0 END) as negativeCnt
    FROM Emotion e
    WHERE e.userId IN :userIds
    GROUP BY e.userId
    """)
    List<EmotionCountProjection> countEmotionByUserIds(@Param("userIds") List<Long> userIds);

    @Query("""
    SELECT new com.example.ballog.domain.emotion.dto.response.TeamEmotionResponse(
        u.baseballTeam,
        SUM(CASE WHEN e.emotionType = 'POSITIVE' THEN 1 ELSE 0 END),
        SUM(CASE WHEN e.emotionType = 'NEGATIVE' THEN 1 ELSE 0 END)
    )
    FROM Emotion e
    JOIN User u ON e.userId = u.userId
    WHERE u.baseballTeam <> 'NONE'
    GROUP BY u.baseballTeam
    """)
    List<TeamEmotionResponse> countEmotionByTeam();

    @Query("""
    SELECT new  com.example.ballog.domain.emotion.dto.response.TeamEmotionResponse(
        :team,
        SUM(CASE WHEN e.emotionType = 'POSITIVE' THEN 1 ELSE 0 END),
        SUM(CASE WHEN e.emotionType = 'NEGATIVE' THEN 1 ELSE 0 END)
    )
    FROM Emotion e
    JOIN e.matches m
    WHERE e.userId = :userId
      AND (m.homeTeam = :team OR m.awayTeam = :team)
    """)
    Optional<TeamEmotionResponse> countMyEmotionByTeam(
            @Param("userId") Long userId,
            @Param("team") BaseballTeam team
    );
}