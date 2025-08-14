package com.example.ballog.domain.matchrecord.controller;

import com.example.ballog.domain.login.entity.BaseballTeam;
import com.example.ballog.domain.login.entity.User;
import com.example.ballog.domain.login.security.CustomUserDetails;
import com.example.ballog.domain.matchrecord.dto.request.MatchResultRequest;
import com.example.ballog.domain.matchrecord.dto.response.MatchRecordDetailResponse;
import com.example.ballog.domain.matchrecord.dto.response.MatchRecordListResponse;
import com.example.ballog.domain.matchrecord.dto.response.MatchRecordSummaryResponse;
import com.example.ballog.domain.matchrecord.entity.MatchRecord;
import com.example.ballog.domain.matchrecord.entity.Result;
import com.example.ballog.domain.matchrecord.service.MatchRecordService;
import com.example.ballog.domain.matchrecord.dto.request.MatchRecordRequest;
import com.example.ballog.domain.matchrecord.dto.response.MatchRecordResponse;
import com.example.ballog.global.common.exception.CustomException;
import com.example.ballog.global.common.exception.enums.ErrorCode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static org.mockito.BDDMockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class MatchRecordControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private MatchRecordService matchRecordService;


    @Test
    void createRecord_성공() throws Exception {
        MatchRecordRequest request = new MatchRecordRequest();
        ReflectionTestUtils.setField(request, "matchesId", 1L);
        ReflectionTestUtils.setField(request, "result", Result.WIN);

        User user = new User();
        user.setUserId(10L);
        user.setEmail("user@example.com");
        LocalDate matchDate = LocalDate.of(2025, 7, 18);
        LocalTime matchTime = LocalTime.of(18, 30);

        MatchRecordResponse response = MatchRecordResponse.builder()
                .matchRecordId(100L)
                .matchesId(1L)
                .homeTeam(BaseballTeam.LG_TWINS)
                .awayTeam(BaseballTeam.KT_WIZ)
                .matchDate(matchDate)
                .matchTime(matchTime)
                .userId(10L)
                .watchCnt(1L)
                .result(Result.WIN)
                .baseballTeam(BaseballTeam.LG_TWINS)
                .positiveEmotionPercent(50.0)
                .negativeEmotionPercent(50.0)
                .defaultImageUrl("http://image-url")
                .build();

        given(matchRecordService.createRecord(any(MatchRecordRequest.class), any(User.class)))
                .willReturn(response);

        CustomUserDetails userDetails = new CustomUserDetails(user);

        mockMvc.perform(post("/api/v1/record")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(request))
                        .with(authentication(new UsernamePasswordAuthenticationToken(userDetails, null))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value("직관 기록 등록 성공"))
                .andExpect(jsonPath("$.data.matchRecordId").value(100))
                .andExpect(jsonPath("$.data.result").value("WIN"))
                .andExpect(jsonPath("$.data.positiveEmotionPercent").value(50.0));
    }

    @Test
    void createRecord_실패() throws Exception {
        MatchRecordRequest request = new MatchRecordRequest();
        ReflectionTestUtils.setField(request, "matchesId", 999L);
        ReflectionTestUtils.setField(request, "result", Result.WIN);

        User user = new User();
        user.setUserId(10L);
        user.setEmail("user@example.com");

        given(matchRecordService.createRecord(any(MatchRecordRequest.class), any(User.class)))
                .willThrow(new CustomException(ErrorCode.MATCH_NOT_FOUND));

        CustomUserDetails userDetails = new CustomUserDetails(user);

        mockMvc.perform(post("/api/v1/record")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(request))
                        .with(authentication(new UsernamePasswordAuthenticationToken(userDetails, null))))
                .andExpect(status().is(408))
                .andExpect(jsonPath("$.message").value("fail"))
                .andExpect(jsonPath("$.status").value(408))
                .andExpect(jsonPath("$.error").value("해당 경기 정보를 찾을 수 없습니다."))
                .andExpect(jsonPath("$.code").value("MATCH001"));
    }

    @Test
    void getRecordDetail_성공() throws Exception {
        Long recordId = 1L;

        User user = new User();
        user.setUserId(100L);
        LocalDate matchDate = LocalDate.of(2025, 7, 18);
        LocalTime matchTime = LocalTime.of(18, 30);


        MatchRecordDetailResponse response = MatchRecordDetailResponse.builder()
                .matchRecordId(recordId)
                .matchesId(5L)
                .homeTeam(BaseballTeam.LG_TWINS)
                .awayTeam(BaseballTeam.KT_WIZ)
                .matchDate(matchDate)
                .matchTime(matchTime)
                .userId(100L)
                .result(Result.WIN)
                .build();

        given(matchRecordService.getRecordDetail(recordId, user)).willReturn(response);

        CustomUserDetails userDetails = new CustomUserDetails(user);

        mockMvc.perform(get("/api/v1/record/{recordId}", recordId)
                        .with(authentication(new UsernamePasswordAuthenticationToken(userDetails, null))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value("직관 기록 상세 조회 성공"))
                .andExpect(jsonPath("$.data.matchRecordId").value(recordId))
                .andExpect(jsonPath("$.data.homeTeam").value("LG_TWINS"));
    }

    @Test
    void getRecordDetail_실패() throws Exception {

        Long recordId = 1L;

        User userNotOwner = new User();
        userNotOwner.setUserId(999L);

        doThrow(new CustomException(ErrorCode.RECORD_NOT_OWNED))
                .when(matchRecordService).getRecordDetail(recordId, userNotOwner);

        CustomUserDetails userDetails = new CustomUserDetails(userNotOwner);

        mockMvc.perform(get("/api/v1/record/{recordId}", recordId)
                        .with(authentication(new UsernamePasswordAuthenticationToken(userDetails, null))))
                .andExpect(status().is(409))
                .andExpect(jsonPath("$.message").value("fail"))
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.error").value("본인이 작성한 기록만 접근할 수 있습니다."))
                .andExpect(jsonPath("$.code").value("RECORD003"));
    }


    @Test
    void getAllRecords_성공() throws Exception {
        User user = new User();
        user.setUserId(10L);
        user.setEmail("user@example.com");
        CustomUserDetails userDetails = new CustomUserDetails(user);
        LocalDate matchDate = LocalDate.of(2025, 7, 18);
        LocalTime matchTime = LocalTime.of(18, 30);

        List<MatchRecordSummaryResponse> records = List.of(
                MatchRecordSummaryResponse.builder()
                        .matchRecordId(1L)
                        .matchesId(1L)
                        .homeTeam(BaseballTeam.LG_TWINS)
                        .awayTeam(BaseballTeam.KT_WIZ)
                        .matchDate(matchDate)
                        .matchTime(matchTime)
                        .userId(10L)
                        .watchCnt(1L)
                        .result(Result.WIN)
                        .baseballTeam(BaseballTeam.LG_TWINS)
                        .build()
        );

        MatchRecordListResponse response = MatchRecordListResponse.builder()
                .totalCount(1)
                .winRate(100.0)
                .totalPositiveEmotionPercent(70.0)
                .totalNegativeEmotionPercent(30.0)
                .records(records)
                .build();

        given(matchRecordService.getAllRecordsByUser(any(User.class))).willReturn(response);

        mockMvc.perform(get("/api/v1/record")
                        .with(authentication(new UsernamePasswordAuthenticationToken(userDetails, null))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value("전체 직관 기록 목록 조회 성공"))
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.data.totalCount").value(1))
                .andExpect(jsonPath("$.data.winRate").value(100.0))
                .andExpect(jsonPath("$.data.totalPositiveEmotionPercent").value(70.0))
                .andExpect(jsonPath("$.data.totalNegativeEmotionPercent").value(30.0))
                .andExpect(jsonPath("$.data.records[0].matchRecordId").value(1))
                .andExpect(jsonPath("$.data.records[0].homeTeam").value("LG_TWINS"));
    }

    @Test
    void getAllRecords_실패() throws Exception {
        User user = new User();
        user.setUserId(10L);
        user.setEmail("user@example.com");
        CustomUserDetails userDetails = new CustomUserDetails(user);

        given(matchRecordService.getAllRecordsByUser(any(User.class)))
                .willThrow(new CustomException(ErrorCode.RECORD_NOT_OWNED));

        mockMvc.perform(get("/api/v1/record")
                        .with(authentication(new UsernamePasswordAuthenticationToken(userDetails, null))))
                .andExpect(status().is(409))
                .andExpect(jsonPath("$.message").value("fail"))
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.error").value("본인이 작성한 기록만 접근할 수 있습니다."))
                .andExpect(jsonPath("$.code").value("RECORD003"));
    }

    @Test
    void deleteRecord_성공() throws Exception {
        Long recordId = 1L;

        User user = new User();
        user.setUserId(100L);
        user.setEmail("user@example.com");

        willDoNothing().given(matchRecordService).deleteRecord(recordId, user);

        CustomUserDetails userDetails = new CustomUserDetails(user);

        mockMvc.perform(delete("/api/v1/record/{recordId}", recordId)
                        .with(authentication(new UsernamePasswordAuthenticationToken(userDetails, null))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value("직관 기록 삭제 성공"))
                .andExpect(jsonPath("$.data").doesNotExist());
    }

    @Test
    void deleteRecord_실패() throws Exception {
        Long recordId = 1L;

        User differentUser = new User();
        differentUser.setUserId(200L);

        CustomUserDetails userDetails = new CustomUserDetails(differentUser);

        willThrow(new CustomException(ErrorCode.RECORD_NOT_OWNED_DELETE))
                .given(matchRecordService)
                .deleteRecord(recordId, differentUser);

        mockMvc.perform(delete("/api/v1/record/{recordId}", recordId)
                        .with(authentication(new UsernamePasswordAuthenticationToken(userDetails, null))))
                .andExpect(status().isConflict()) // 409
                .andExpect(jsonPath("$.message").value("fail"))
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.error").value("본인이 작성한 기록만 삭제 할 수 있습니다."))
                .andExpect(jsonPath("$.code").value("RECORD002"));
    }
}