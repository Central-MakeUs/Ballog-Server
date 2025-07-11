package com.example.ballog.domain.Image.controller;

import com.example.ballog.domain.Image.dto.response.ImageSaveResponse;
import com.example.ballog.domain.Image.entity.Image;
import com.example.ballog.domain.Image.service.S3Service;
import com.example.ballog.domain.Image.dto.request.ImageSaveRequest;
import com.example.ballog.domain.Image.service.ImageService;
import com.example.ballog.domain.login.security.CustomUserDetails;
import com.example.ballog.global.common.exception.CustomException;
import com.example.ballog.global.common.exception.enums.ErrorCode;
import com.example.ballog.global.common.message.ApiErrorResponse;
import com.example.ballog.global.common.message.ApiErrorResponses;
import com.example.ballog.global.common.message.BasicResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/image")
@Tag(name = "Image", description = "Image API")
public class ImageController {
    private final S3Service s3Service;
    private final ImageService imageService;

    @GetMapping("/presigned-url")
    public ResponseEntity<S3Service.PresignedUrlResponse> getPresignedUrl(
            @RequestParam("originalFileName") String originalFileName
    ) {
        return ResponseEntity.ok(s3Service.generatePresignedUrl(originalFileName));
    }

    @PostMapping
    @Operation(summary = "이미지 저장", description = "S3에 업로드된 이미지 정보를 DB에 저장하는 API")
    @ApiErrorResponses({
            @ApiErrorResponse(ErrorCode.UNAUTHORIZED),
            @ApiErrorResponse(ErrorCode.NOT_FOUND_RECORD)
    })
    public ResponseEntity<BasicResponse<ImageSaveResponse>> saveImage(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody ImageSaveRequest request
    ) {
        if (userDetails == null) {
            throw new CustomException(ErrorCode.UNAUTHORIZED);
        }

        Long userId = userDetails.getUser().getUserId();

        Image savedImage = imageService.saveImage(request, userId);
        ImageSaveResponse response = imageService.toImageSaveResponse(savedImage);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(BasicResponse.ofSuccess("이미지 저장 성공", HttpStatus.CREATED.value(), response));
    }
}
