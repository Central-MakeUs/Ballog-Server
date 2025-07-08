package com.example.ballog.global.common.message;

import com.example.ballog.global.common.exception.enums.ErrorCode;

import java.lang.annotation.*;

@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Repeatable(ApiErrorResponses.class)
public @interface ApiErrorResponse {
    ErrorCode value();
}