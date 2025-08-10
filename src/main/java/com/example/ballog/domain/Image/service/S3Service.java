package com.example.ballog.domain.Image.service;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.GeneratePresignedUrlRequest;
import com.example.ballog.global.common.exception.CustomException;
import com.example.ballog.global.common.exception.enums.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Value;

import java.net.URL;
import java.time.Instant;
import java.util.Date;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class S3Service {

    private final AmazonS3 amazonS3;

    @Value("${cloud.aws.s3.bucket}")
    private String bucket;
    @Value("${cloud.aws.region.static}")
    private String region;

    public PresignedUrlResponse generatePresignedUrl(String originalFileName) {

        String extension = getExtension(originalFileName);
        if (extension == null) {
            throw new CustomException(ErrorCode.INVALID_FILE_EXTENSION);
        }

        String mimeType = getMimeTypeByExtension(extension);
        if (mimeType == null) {
            throw new CustomException(ErrorCode.UNSUPPORTED_FILE_EXTENSION);
        }

        String uuidFileName = UUID.randomUUID() + "." + extension;
        Date expiration = Date.from(Instant.now().plusSeconds(60 * 15));
        String s3Key = "images/" + uuidFileName;

        GeneratePresignedUrlRequest generatePresignedUrlRequest = new GeneratePresignedUrlRequest(bucket, s3Key)
                .withMethod(com.amazonaws.HttpMethod.PUT)
                .withExpiration(expiration);

        URL url = amazonS3.generatePresignedUrl(generatePresignedUrlRequest);

        return new PresignedUrlResponse(url.toString(), uuidFileName);
    }

    private String getExtension(String fileName) {
        int dotIndex = fileName.lastIndexOf('.');
        if (dotIndex == -1) return null;
        return fileName.substring(dotIndex + 1).toLowerCase();
    }

    private String getMimeTypeByExtension(String extension) {
        return switch (extension) {
            case "png" -> "image/png";
            case "jpg", "jpeg" -> "image/jpeg";
            case "gif" -> "image/gif";
            case "bmp" -> "image/bmp";
            case "svg" -> "image/svg+xml";
            case "webp" -> "image/webp";
            default -> null;
        };
    }

    public record PresignedUrlResponse(String presignedUrl, String fileName) {}


    public String extractFileNameFromUrl(String url) {
        if (url == null || url.isEmpty()) {
            throw new CustomException(ErrorCode.INVALID_URL);
        }
        return url.substring(url.lastIndexOf('/') + 1);
    }
    public String getAccessibleUrl(String fileName) {
        return "https://" + bucket + ".s3." + region + ".amazonaws.com/images/" + fileName;
    }

    public void deleteFileFromS3(String imageUrl) { //s3 버킷에 올라간 이미지 삭제
        if (imageUrl == null || imageUrl.isEmpty()) {
            throw new CustomException(ErrorCode.INVALID_URL);
        }

        String fileName = extractFileNameFromUrl(imageUrl);

        String s3Key = "images/" + fileName;

        try {
            amazonS3.deleteObject(bucket, s3Key);
        } catch (Exception e) {
            throw new CustomException(ErrorCode.S3_DELETE_FAILED); // 필요 시 ErrorCode에 정의
        }
    }



}

