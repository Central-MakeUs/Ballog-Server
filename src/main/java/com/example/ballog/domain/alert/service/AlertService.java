package com.example.ballog.domain.alert.service;

import com.example.ballog.domain.alert.dto.request.AlertUpdateRequest;
import com.example.ballog.domain.alert.dto.response.AlertResponse;
import com.example.ballog.domain.alert.entity.Alert;
import com.example.ballog.domain.alert.repository.AlertRepository;
import com.example.ballog.domain.login.repository.UserRepository;
import com.example.ballog.global.common.exception.CustomException;
import com.example.ballog.global.common.exception.enums.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AlertService {

    private final AlertRepository alertRepository;
    private final UserRepository userRepository;

    @Transactional
    public AlertResponse updateAlert(Long userId, AlertUpdateRequest request) {
        Alert alert = alertRepository.findByUser_UserId(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.ALERT_NOT_FOUND));

        if (request.getStartAlert() != null) {
            alert.setStartAlert(request.getStartAlert());
        }

        if (request.getInGameAlert() != null) {
            alert.setInGameAlert(request.getInGameAlert());
        }

        return AlertResponse.from(alert);
    }
}
