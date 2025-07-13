package com.example.ballog.domain.Image.service;

import com.example.ballog.domain.Image.dto.request.ImageSaveRequest;
import com.example.ballog.domain.Image.dto.response.ImageSaveResponse;
import com.example.ballog.domain.Image.entity.Image;
import com.example.ballog.domain.matchrecord.entity.MatchRecord;
import com.example.ballog.domain.matchrecord.repository.MatchRecordRepository;
import com.example.ballog.domain.Image.respository.ImageRepository;
import com.example.ballog.global.common.exception.CustomException;
import com.example.ballog.global.common.exception.enums.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class ImageService {
    private final ImageRepository imageRepository;
    private final MatchRecordRepository matchRecordRepository;
    private final S3Service s3Service;

    @Value("${app.default-image-url}")
    private String defaultImageUrl;

    @Transactional
    public Image saveImage(ImageSaveRequest request, Long userId) {
        MatchRecord matchRecord = matchRecordRepository.findById(request.getRecordId())
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_RECORD));

        String fileName = s3Service.extractFileNameFromUrl(request.getImageUrl());
        String accessibleUrl = s3Service.getAccessibleUrl(fileName);

        Image image = Image.builder()
                .matchRecord(matchRecord)
                .matchesId(matchRecord.getMatches().getMatchesId())
                .userId(userId)
                .imageUrl(accessibleUrl)
                .createdAt(LocalDateTime.now())
                .build();

        imageRepository.save(image);

        if (matchRecord.getDefaultImageUrl().equals(defaultImageUrl)) {
            String firstImageUrl = imageRepository
                    .findFirstByMatchRecordOrderByCreatedAtAsc(matchRecord)
                    .map(Image::getImageUrl)
                    .orElse(null);

            if (firstImageUrl != null) {
                matchRecord.setDefaultImageUrl(firstImageUrl);
            }
        }

        return image;
    }

    public ImageSaveResponse toImageSaveResponse(Image image) {
        return ImageSaveResponse.builder()
                .imageId(image.getImageId())
                .imageUrl(image.getImageUrl())
                .createdAt(image.getCreatedAt())
                .userId(image.getUserId())
                .matchesId(image.getMatchesId())
                .recordId(image.getMatchRecord().getMatchrecordId())
                .build();
    }
}
