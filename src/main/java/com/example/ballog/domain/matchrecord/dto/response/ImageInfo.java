package com.example.ballog.domain.matchrecord.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class ImageInfo {
    private String imageUrl;
    private LocalDateTime createdAt;
}
