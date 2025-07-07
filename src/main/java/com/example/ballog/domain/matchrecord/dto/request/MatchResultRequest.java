package com.example.ballog.domain.matchrecord.dto.request;

import com.example.ballog.domain.matchrecord.entity.Result;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MatchResultRequest {
    @NotNull
    private Result result;
}
