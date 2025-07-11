package com.example.ballog.domain.Image.dto.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ImageSaveRequest {
    private Long recordId;
    private String imageUrl;
}