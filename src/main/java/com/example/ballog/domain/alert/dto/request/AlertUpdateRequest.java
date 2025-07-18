package com.example.ballog.domain.alert.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AlertUpdateRequest {
    private Boolean startAlert;
    private Boolean inGameAlert;
}
