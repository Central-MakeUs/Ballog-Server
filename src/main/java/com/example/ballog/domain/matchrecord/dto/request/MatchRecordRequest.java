package com.example.ballog.domain.matchrecord.dto.request;

import com.example.ballog.domain.matchrecord.entity.Result;
import lombok.Getter;

@Getter
public class MatchRecordRequest {
    private Long matchesId;
    private Result result;
}
