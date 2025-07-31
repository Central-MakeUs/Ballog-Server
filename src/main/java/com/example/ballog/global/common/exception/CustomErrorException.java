package com.example.ballog.global.common.exception;

import com.example.ballog.global.common.message.ErrorResponse;
import lombok.Getter;


@Getter
public class CustomErrorException extends RuntimeException{
    private ErrorResponse errorResponse;
    public CustomErrorException(ErrorResponse errorResponse){
        this.errorResponse = errorResponse;
    }
}
