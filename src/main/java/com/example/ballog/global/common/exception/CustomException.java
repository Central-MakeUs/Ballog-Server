package com.example.ballog.global.common.exception;

import com.example.ballog.global.common.exception.enums.ErrorCode;
import lombok.Getter;

@Getter
public class CustomException extends RuntimeException{

    private ErrorCode errorCode;
    public CustomException(ErrorCode errorCode){
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }

}
