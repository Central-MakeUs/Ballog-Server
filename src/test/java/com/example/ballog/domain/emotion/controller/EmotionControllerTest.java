package com.example.ballog.domain.emotion.controller;

import com.example.ballog.domain.emotion.dto.request.EmotionEnrollRequest;
import com.example.ballog.domain.emotion.dto.response.EmotionResponse;
import com.example.ballog.domain.emotion.entity.EmotionType;
import com.example.ballog.domain.emotion.service.EmotionService;
import com.example.ballog.domain.login.entity.BaseballTeam;
import com.example.ballog.domain.login.entity.User;
import com.example.ballog.domain.login.security.CustomUserDetails;
import com.example.ballog.domain.match.entity.Stadium;
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

import java.time.LocalDate;
import java.time.LocalTime;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
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
class EmotionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private EmotionService emotionService;


    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void 감정등록_성공() throws Exception {
        Long userId = 1L;
        Long matchRecordId = 100L;

        EmotionEnrollRequest request = new EmotionEnrollRequest();
        request.setMatchRecordId(matchRecordId);
        request.setEmotionType(EmotionType.POSITIVE);

        EmotionResponse response = EmotionResponse.builder()
                .matchesDate(LocalDate.of(2025, 8, 2))
                .matchesTime(LocalTime.of(19, 30))
                .homeTeam(BaseballTeam.LG_TWINS)
                .awayTeam(BaseballTeam.KT_WIZ)
                .stadium(Stadium.JAMSIL)
                .positivePercent(80.0)
                .negativePercent(20.0)
                .recentEmotion(EmotionType.POSITIVE)
                .defaultImageUrl("http://example.com/image.jpg")
                .build();

        given(emotionService.createEmotion(any(EmotionEnrollRequest.class), eq(userId)))
                .willReturn(response);


        CustomUserDetails userDetails = mock(CustomUserDetails.class);
        User mockUser = new User();
        mockUser.setUserId(userId);
        when(userDetails.getUser()).thenReturn(mockUser);


        mockMvc.perform(post("/api/v1/emotion")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(request))
                        .principal(() -> "user")
                        .with(authentication(new TestingAuthenticationToken(userDetails, null))))
                .andExpect(jsonPath("$.message").value("success"))
                .andExpect(jsonPath("$.success").value("감정 표현 등록 성공"))
                .andExpect(jsonPath("$.data.homeTeam").value("LG_TWINS"))
                .andExpect(jsonPath("$.data.positivePercent").value(80.0));
    }



    @Test
    void 감정등록_실패() throws Exception {

        Long matchRecordId = 100L;
        Long userId = 1L;

        EmotionEnrollRequest request = new EmotionEnrollRequest(matchRecordId, EmotionType.POSITIVE);

        CustomUserDetails userDetails = mock(CustomUserDetails.class);
        User mockUser = new User();
        mockUser.setUserId(userId);
        when(userDetails.getUser()).thenReturn(mockUser);

        given(emotionService.createEmotion(any(EmotionEnrollRequest.class), eq(userId)))
                .willThrow(new CustomException(ErrorCode.RECORD_NOT_OWNED));

        mockMvc.perform(post("/api/v1/emotion")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .principal(() -> "user")
                        .with(authentication(new TestingAuthenticationToken(userDetails, null))))
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.message").value("fail"))
                .andExpect(jsonPath("$.code").value("RECORD003"))
                .andExpect(jsonPath("$.error").value("본인이 작성한 기록만 접근할 수 있습니다."));
    }

    @Test
    void 감정비율조회_성공() throws Exception {
        Long recordId = 100L;
        Long userId = 1L;

        EmotionResponse response = EmotionResponse.builder()
                .matchesDate(LocalDate.of(2025, 8, 2))
                .matchesTime(LocalTime.of(19, 30))
                .homeTeam(BaseballTeam.LG_TWINS)
                .awayTeam(BaseballTeam.KT_WIZ)
                .stadium(Stadium.JAMSIL)
                .positivePercent(75.0)
                .negativePercent(25.0)
                .recentEmotion(EmotionType.POSITIVE)
                .defaultImageUrl("http://example.com/image.jpg")
                .build();

        given(emotionService.getEmotionRatio(recordId, userId)).willReturn(response);

        CustomUserDetails userDetails = mock(CustomUserDetails.class);
        User mockUser = new User();
        mockUser.setUserId(userId);
        when(userDetails.getUser()).thenReturn(mockUser);

        mockMvc.perform(get("/api/v1/emotion/{recordId}", recordId)
                        .principal(() -> "user")
                        .with(authentication(new TestingAuthenticationToken(userDetails, null))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("success"))
                .andExpect(jsonPath("$.success").value("감정 비율 조회 성공"))
                .andExpect(jsonPath("$.data.homeTeam").value("LG_TWINS"))
                .andExpect(jsonPath("$.data.positivePercent").value(75.0));
    }

    @Test
    void 감정비율조회_실패() throws Exception {
        Long recordId = 100L;
        Long userId = 1L;

        CustomUserDetails userDetails = mock(CustomUserDetails.class);
        User mockUser = new User();
        mockUser.setUserId(userId);
        when(userDetails.getUser()).thenReturn(mockUser);

        given(emotionService.getEmotionRatio(recordId, userId))
                .willThrow(new CustomException(ErrorCode.NOT_FOUND_RECORD));

        mockMvc.perform(get("/api/v1/emotion/{recordId}", recordId)
                        .principal(() -> "user")
                        .with(authentication(new TestingAuthenticationToken(userDetails, null))))
                .andExpect(jsonPath("$.status").value(408))
                .andExpect(jsonPath("$.message").value("fail"))
                .andExpect(jsonPath("$.code").value("RECORD001"))
                .andExpect(jsonPath("$.error").value("해당 직관기록을 찾을 수 없습니다."));
    }



    private static String asJsonString(final Object obj) {
        try {
            return new ObjectMapper().writeValueAsString(obj);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}