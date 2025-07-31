package com.example.ballog.domain.alert.dto.response;

import com.example.ballog.domain.alert.entity.Alert;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AlertResponse {
    private Boolean startAlert;
    private Boolean inGameAlert;

    public AlertResponse(Boolean startAlert, Boolean inGameAlert) {
        this.startAlert = startAlert;
        this.inGameAlert = inGameAlert;
    }

    public static AlertResponse from(Alert alert) {
        return new AlertResponse(alert.getStartAlert(), alert.getInGameAlert());
    }
}
