package com.example.ballog.domain.Image.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class ImageSaveResponse {
    private Long imageId;
    private String imageUrl;
    private LocalDateTime createdAt;
    private Long userId;
    private Long matchesId;
    private Long recordId;
}
