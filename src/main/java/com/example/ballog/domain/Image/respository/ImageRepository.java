package com.example.ballog.domain.Image.respository;


import com.example.ballog.domain.Image.entity.Image;
import com.example.ballog.domain.matchrecord.entity.MatchRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ImageRepository  extends JpaRepository<Image, Long> {
    Optional<Image> findFirstByMatchRecordOrderByCreatedAtAsc(MatchRecord matchRecord);
    List<Image> findByMatchRecord(MatchRecord matchRecord);
    @Modifying
    @Query("delete from Image i where i.matchRecord.user.userId = :userId")
    void deleteAllByUserUserId(@Param("userId") Long userId);

}
