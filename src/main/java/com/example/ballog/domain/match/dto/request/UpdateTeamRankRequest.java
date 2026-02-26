package com.example.ballog.domain.match.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UpdateTeamRankRequest {
    @NotBlank(message = "팀 코드는 필수입니다.")
    private String teamCode;

    @NotNull(message = "순위는 필수입니다.")
    @Min(value = 1, message = "순위는 1 이상이어야 합니다.")
    private Integer newRank;

}
