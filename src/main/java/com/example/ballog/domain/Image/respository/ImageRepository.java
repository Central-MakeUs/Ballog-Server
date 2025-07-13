package com.example.ballog.domain.Image.respository;


import com.example.ballog.domain.Image.entity.Image;
import com.example.ballog.domain.matchrecord.entity.MatchRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ImageRepository  extends JpaRepository<Image, Long> {
    Optional<Image> findFirstByMatchRecordOrderByCreatedAtAsc(MatchRecord matchRecord);

}
