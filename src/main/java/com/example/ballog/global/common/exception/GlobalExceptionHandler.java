package com.example.ballog.global.common.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import com.example.ballog.global.common.message.ErrorResponse;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {
    @ExceptionHandler(CustomException.class)
    public ResponseEntity<ErrorResponse> handleCustomException(CustomException ex) {
        ErrorResponse response = new ErrorResponse(ex.getErrorCode());
        return new ResponseEntity<>(response, HttpStatus.valueOf(ex.getErrorCode().getStatus()));
    }

    @ExceptionHandler(CustomErrorException.class)
    public ResponseEntity<ErrorResponse> handleCustomErrorException(CustomErrorException ex) {
        ErrorResponse response = new ErrorResponse(ex.getErrorResponse().getStatus(), ex.getErrorResponse().getCode(), ex.getErrorResponse().getError());
        return new ResponseEntity<>(response, HttpStatus.valueOf(ex.getErrorResponse().getStatus()));
    }
}