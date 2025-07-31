package com.example.ballog.global.common.message;


import com.example.ballog.global.common.exception.enums.ErrorCode;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpStatus;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class BasicResponse<T> {
    private String message;
    private Integer status;
    private String success;
    private T data;

    private static final String DEFAULT_MESSAGE = "success";

    public static <T> BasicResponse<T> of(HttpStatus status, String successMessage, T data) {
        return new BasicResponse<>(DEFAULT_MESSAGE, status.value(), successMessage, data);
    }

    public static <T> BasicResponse<T> ofSuccess(String successMessage) {
        return new BasicResponse<>(DEFAULT_MESSAGE, HttpStatus.OK.value(), successMessage, null);
    }

    public static <T> BasicResponse<T> ofSuccess(String successMessage, T data) {
        return new BasicResponse<>(DEFAULT_MESSAGE, HttpStatus.OK.value() , successMessage, data);
    }

    public static <T> BasicResponse<T> ofSuccess(T data, String successMessage) {
        return new BasicResponse<>(DEFAULT_MESSAGE, HttpStatus.OK.value(), successMessage, data);
    }

    public static <T> BasicResponse<T> ofFailure(String errorMessage, HttpStatus status) {
        return new BasicResponse<>("fail", status.value(), errorMessage, null);
    }

    public static <T> BasicResponse<T> ofFailure(ErrorCode errorCode) {
        return new BasicResponse<>("fail", errorCode.getStatus(), errorCode.getMessage(), null);
    }
}
