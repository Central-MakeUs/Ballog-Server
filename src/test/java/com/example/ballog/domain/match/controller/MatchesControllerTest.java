package com.example.ballog.domain.match.controller;

import com.example.ballog.domain.login.entity.Role;
import com.example.ballog.domain.login.entity.User;
import com.example.ballog.domain.login.security.CustomUserDetails;
import com.example.ballog.domain.match.dto.request.MatchesRequest;
import com.example.ballog.domain.match.dto.response.MatchesGroupedResponse;
import com.example.ballog.domain.match.dto.response.MatchesResponse;
import com.example.ballog.domain.match.dto.response.MatchesWithResponse;
import com.example.ballog.domain.match.entity.Matches;
import com.example.ballog.domain.match.entity.Stadium;
import com.example.ballog.domain.match.service.MatchesService;
import com.example.ballog.global.common.exception.CustomException;
import com.example.ballog.global.common.exception.enums.ErrorCode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.Test;

import com.example.ballog.domain.login.entity.BaseballTeam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.test.web.servlet.MockMvc;


import java.time.LocalDate;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willDoNothing;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@SpringBootTest
@AutoConfigureMockMvc
class MatchesControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private MatchesService matchesService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void createMatch_성공() throws Exception {
        MatchesRequest request = new MatchesRequest();
        request.setMatchesDate(LocalDate.of(2025, 7, 21));
        request.setMatchesTime(LocalTime.of(18, 30));
        request.setHomeTeam(BaseballTeam.LG_TWINS);
        request.setAwayTeam(BaseballTeam.KT_WIZ);
        request.setStadium(Stadium.JAMSIL);

        MatchesResponse response = new MatchesResponse(
                1L,
                request.getMatchesDate(),
                request.getMatchesTime(),
                request.getHomeTeam(),
                request.getAwayTeam(),
                request.getStadium(),
                request.getMatchesResult()
        );

        Matches matches = new Matches();

        MatchesWithResponse matchesWithResponse = new MatchesWithResponse(matches, response);

        given(matchesService.createMatches(any(MatchesRequest.class)))
                .willReturn(matchesWithResponse);

        mockMvc.perform(post("/api/v1/match")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .with(authentication(authUser(Role.ADMIN))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.matchesId").value(1L))
                .andExpect(jsonPath("$.message").value("success"))
                .andExpect(jsonPath("$.success").value("경기일정이 등록 성공"));
    }


    @Test
    void createMatch_실패() throws Exception {
        MatchesRequest request = new MatchesRequest();
        request.setMatchesDate(LocalDate.of(2025, 8, 1));
        request.setMatchesTime(LocalTime.of(18, 30));
        request.setHomeTeam(BaseballTeam.KT_WIZ);
        request.setAwayTeam(BaseballTeam.LG_TWINS);
        request.setStadium(Stadium.JAMSIL);

        mockMvc.perform(post("/api/v1/match")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .with(authentication(authUser(Role.USER))))
                .andExpect(status().isNotAcceptable())
                .andExpect(jsonPath("$.message").value("fail"))
                .andExpect(jsonPath("$.status").value(406))
                .andExpect(jsonPath("$.error").value("접근 권한이 없습니다."))
                .andExpect(jsonPath("$.code").value("ROLE001"));

    }

    private Authentication authUser(Role role) {
        User user = new User();
        user.setUserId(1L);
        user.setEmail("admin@test.com");
        user.setRole(role);

        CustomUserDetails userDetails = new CustomUserDetails(user);
        return new UsernamePasswordAuthenticationToken(userDetails, "", userDetails.getAuthorities());
    }

    @Test
    void getTodayMatches_성공() throws Exception {
        List<MatchesResponse> mockResponse = List.of(
                new MatchesResponse(1L, LocalDate.now(), LocalTime.of(18, 30), BaseballTeam.LG_TWINS, BaseballTeam.KT_WIZ, Stadium.JAMSIL, null),
                new MatchesResponse(2L, LocalDate.now(), LocalTime.of(20, 0), BaseballTeam.DOOSAN_BEARS, BaseballTeam.SSG_LANDERS, Stadium.GWANGJU, null)
        );

        given(matchesService.getTodayMatches())
                .willReturn(mockResponse);

        mockMvc.perform(get("/api/v1/match")
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(authentication(authUser(Role.USER))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("success"))
                .andExpect(jsonPath("$.success").value("오늘 경기 일정 조회 성공"))
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(mockResponse.size()))
                .andExpect(jsonPath("$.data[0].matchesId").value(1L))
                .andExpect(jsonPath("$.data[1].homeTeam").value("DOOSAN_BEARS"));

    }

    @Test
    void getTodayMatches_실패() throws Exception {
        given(matchesService.getTodayMatches())
                .willThrow(new CustomException(ErrorCode.INTERNAL_SERVER_ERROR));

        mockMvc.perform(get("/api/v1/match")
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(authentication(authUser(Role.USER))))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.message").value("fail"))
                .andExpect(jsonPath("$.status").value(500))
                .andExpect(jsonPath("$.error").value("서버 내부 오류가 발생했습니다."))
                .andExpect(jsonPath("$.code").value("INTERNAL_SERVER_ERROR"));
    }

    @Test
    void getAllMatchesGroupedByDate_성공() throws Exception {

        Map<String, List<MatchesGroupedResponse>> mockResponse = new HashMap<>();
        mockResponse.put("2025-08-01", List.of(
                new MatchesGroupedResponse(1L, LocalTime.of(18, 30), BaseballTeam.LG_TWINS, BaseballTeam.KT_WIZ, Stadium.JAMSIL,null)
        ));
        mockResponse.put("2025-08-02", List.of(
                new MatchesGroupedResponse(2L, LocalTime.of(19, 0), BaseballTeam.DOOSAN_BEARS, BaseballTeam.SSG_LANDERS, Stadium.GWANGJU,null)
        ));

        given(matchesService.getAllMatchesGroupedByDate())
                .willReturn(mockResponse);

        mockMvc.perform(get("/api/v1/match/all")
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(authentication(authUser(Role.USER))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("success"))
                .andExpect(jsonPath("$.success").value("전체 경기 일정 조회 성공"))
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.data['2025-08-01']").isArray())
                .andExpect(jsonPath("$.data['2025-08-01'][0].matchesId").value(1L))
                .andExpect(jsonPath("$.data['2025-08-02'][0].homeTeam").value("DOOSAN_BEARS"));
    }

    @Test
    void getAllMatchesGroupedByDate_실패() throws Exception {

        given(matchesService.getAllMatchesGroupedByDate())
                .willThrow(new CustomException(ErrorCode.INTERNAL_SERVER_ERROR));

        mockMvc.perform(get("/api/v1/match/all")
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(authentication(authUser(Role.USER))))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.message").value("fail"))
                .andExpect(jsonPath("$.status").value(500))
                .andExpect(jsonPath("$.error").value("서버 내부 오류가 발생했습니다."))
                .andExpect(jsonPath("$.code").value("INTERNAL_SERVER_ERROR"));
    }

    @Test
    void getMatchDetail_성공() throws Exception {
        Long matchId = 1L;
        MatchesResponse mockResponse = new MatchesResponse(
                matchId,
                LocalDate.of(2025, 8, 1),
                LocalTime.of(18, 30),
                BaseballTeam.LG_TWINS,
                BaseballTeam.KT_WIZ,
                Stadium.JAMSIL,
               null
        );

        given(matchesService.getMatchDetail(matchId)).willReturn(mockResponse);

        mockMvc.perform(get("/api/v1/match/{matchId}", matchId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(authentication(authUser(Role.USER))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("success"))
                .andExpect(jsonPath("$.success").value("경기 상세 조회 성공"))
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.data.matchesId").value(1L))
                .andExpect(jsonPath("$.data.homeTeam").value("LG_TWINS"))
                .andExpect(jsonPath("$.data.awayTeam").value("KT_WIZ"))
                .andExpect(jsonPath("$.data.stadium").value("JAMSIL"));
    }


    @Test
    void getMatchDetail_실패() throws Exception {
        Long invalidMatchId = 999L;

        given(matchesService.getMatchDetail(invalidMatchId))
                .willThrow(new CustomException(ErrorCode.MATCH_NOT_FOUND));

        mockMvc.perform(get("/api/v1/match/{matchId}", invalidMatchId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(authentication(authUser(Role.USER))))
                .andExpect(status().is(408))
                .andExpect(jsonPath("$.message").value("fail"))
                .andExpect(jsonPath("$.status").value(408))
                .andExpect(jsonPath("$.code").value("MATCH001"))
                .andExpect(jsonPath("$.error").value("해당 경기 정보를 찾을 수 없습니다."));
    }

    @Test
    void updateMatch_성공() throws Exception {
        Long matchId = 1L;

        MatchesRequest request = new MatchesRequest();
        request.setMatchesDate(LocalDate.of(2025, 8, 1));
        request.setMatchesTime(LocalTime.of(18, 30));
        request.setHomeTeam(BaseballTeam.LG_TWINS);
        request.setAwayTeam(BaseballTeam.KT_WIZ);
        request.setStadium(Stadium.JAMSIL);
        request.setMatchesResult("5:3");

        MatchesResponse response = new MatchesResponse(
                matchId,
                request.getMatchesDate(),
                request.getMatchesTime(),
                request.getHomeTeam(),
                request.getAwayTeam(),
                request.getStadium(),
                request.getMatchesResult()
        );

        given(matchesService.updateMatch(eq(matchId), any(MatchesRequest.class)))
                .willReturn(response);

        ObjectMapper objectMapper = new ObjectMapper()
                .registerModule(new JavaTimeModule())
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        mockMvc.perform(patch("/api/v1/match/{matchId}", matchId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .with(authentication(authUser(Role.ADMIN))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("success"))
                .andExpect(jsonPath("$.success").value("경기일정 수정 성공"))
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.data.matchesId").value(matchId))
                .andExpect(jsonPath("$.data.matchesResult").value("5:3"));
    }


    @Test
    void updateMatch_실패() throws Exception {
        Long matchId = 1L;

        MatchesRequest request = new MatchesRequest();
        request.setMatchesDate(LocalDate.of(2025, 8, 1));
        request.setMatchesTime(LocalTime.of(18, 30));
        request.setHomeTeam(BaseballTeam.LG_TWINS);
        request.setAwayTeam(BaseballTeam.KT_WIZ);
        request.setStadium(Stadium.JAMSIL);
        request.setMatchesResult("INVALID_RESULT");

        given(matchesService.updateMatch(eq(matchId), any(MatchesRequest.class)))
                .willThrow(new CustomException(ErrorCode.MATCH_RESULT_FORMAT_INVALID));

        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        mockMvc.perform(patch("/api/v1/match/{matchId}", matchId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .with(authentication(authUser(Role.ADMIN))))
                .andExpect(status().is(413))
                .andExpect(jsonPath("$.message").value("fail"))
                .andExpect(jsonPath("$.status").value(413))
                .andExpect(jsonPath("$.code").value("MATCH002"))
                .andExpect(jsonPath("$.error").value("경기 결과 형식이 잘못되었습니다. 예: 5:3"));
    }


    @Test
    void deleteMatch_성공() throws Exception {
        Long matchId = 1L;

        willDoNothing().given(matchesService).deleteMatch(matchId);

        mockMvc.perform(delete("/api/v1/match/{matchId}", matchId)
                        .with(authentication(authUser(Role.ADMIN)))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("success"))
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.success").value("요청 성공"));
    }


    @Test
    void deleteMatch_실패_UNAUTHORIZED() throws Exception {
        Long matchId = 1L;

        mockMvc.perform(delete("/api/v1/match/{matchId}", matchId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is(404))
                .andExpect(jsonPath("$.message").value("fail"))
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.code").value("AUTH001"))
                .andExpect(jsonPath("$.error").value("인증 정보가 없습니다."));
    }

}