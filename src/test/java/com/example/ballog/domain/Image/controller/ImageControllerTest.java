package com.example.ballog.domain.Image.controller;

import com.example.ballog.domain.Image.dto.request.ImageSaveRequest;
import com.example.ballog.domain.Image.dto.response.ImageSaveResponse;
import com.example.ballog.domain.Image.entity.Image;
import com.example.ballog.domain.Image.service.ImageService;
import com.example.ballog.domain.login.entity.User;
import com.example.ballog.domain.login.security.CustomUserDetails;
import com.example.ballog.domain.matchrecord.entity.MatchRecord;
import com.example.ballog.global.common.exception.CustomException;
import com.example.ballog.global.common.exception.enums.ErrorCode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class ImageControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ImageService imageService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void 이미지저장_성공() throws Exception {
        Long userId = 1L;

        ImageSaveRequest request = new ImageSaveRequest();
        request.setImageUrl("https://example.com/image.jpg");
        request.setMatchRecordId(100L);

        MatchRecord matchRecord = new MatchRecord();
        matchRecord.setMatchrecordId(request.getMatchRecordId());

        Image savedImage = new Image();
        savedImage.setImageId(1L);
        savedImage.setImageUrl(request.getImageUrl());
        savedImage.setMatchRecord(matchRecord);
        savedImage.setUserId(userId);
        savedImage.setCreatedAt(LocalDateTime.now());

        ImageSaveResponse response = ImageSaveResponse.builder()
                .imageId(savedImage.getImageId())
                .imageUrl(savedImage.getImageUrl())
                .createdAt(savedImage.getCreatedAt())
                .userId(savedImage.getUserId())
                .matchRecordId(savedImage.getMatchRecord().getMatchrecordId())
                .build();

        given(imageService.saveImage(any(ImageSaveRequest.class), eq(userId)))
                .willReturn(savedImage);
        given(imageService.toImageSaveResponse(savedImage))
                .willReturn(response);

        CustomUserDetails userDetails = mock(CustomUserDetails.class);
        User mockUser = new User();
        mockUser.setUserId(userId);
        when(userDetails.getUser()).thenReturn(mockUser);

        mockMvc.perform(post("/api/v1/image")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .principal(() -> "user")
                        .with(authentication(new TestingAuthenticationToken(userDetails, null))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.message").value("success"))
                .andExpect(jsonPath("$.success").value("이미지 저장 성공"))
                .andExpect(jsonPath("$.data.imageUrl").value("https://example.com/image.jpg"))
                .andExpect(jsonPath("$.data.matchRecordId").value(100));
    }


    @Test
    void 이미지저장_실패() throws Exception {
        Long userId = 1L;

        ImageSaveRequest request = new ImageSaveRequest();
        request.setImageUrl("https://example.com/image.jpg");
        request.setMatchRecordId(100L);

        CustomUserDetails userDetails = mock(CustomUserDetails.class);
        User mockUser = new User();
        mockUser.setUserId(userId);
        when(userDetails.getUser()).thenReturn(mockUser);

        given(imageService.saveImage(any(ImageSaveRequest.class), eq(userId)))
                .willThrow(new CustomException(ErrorCode.NOT_FOUND_RECORD));

        mockMvc.perform(post("/api/v1/image")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .principal(() -> "user")
                        .with(authentication(new TestingAuthenticationToken(userDetails, null))))
                .andExpect(jsonPath("$.status").value(408))
                .andExpect(jsonPath("$.message").value("fail"))
                .andExpect(jsonPath("$.code").value("RECORD001"))
                .andExpect(jsonPath("$.error").value("해당 직관기록을 찾을 수 없습니다."));
    }



}