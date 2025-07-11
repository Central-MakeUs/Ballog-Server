package com.example.ballog.domain.Image.entity;

import com.example.ballog.domain.matchrecord.entity.MatchRecord;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "image", schema = "ballog")
public class Image {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long imageId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "record_id")
    private MatchRecord matchRecord;

    @Column(name = "matches_id", nullable = false)
    private Long matchesId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    private String imageUrl;

    private LocalDateTime createdAt;
}
