package com.example.ballog.global.common.message;

import com.example.ballog.global.common.exception.enums.ErrorCode;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class ErrorResponse {

    private String message;
    private int status;
    private String error;
    private String code;

    public ErrorResponse(ErrorCode errorCode){
        this.message = "fail";
        this.status = errorCode.getStatus();
        this.error = errorCode.getMessage();
        this.code = errorCode.getCode();
    }
}