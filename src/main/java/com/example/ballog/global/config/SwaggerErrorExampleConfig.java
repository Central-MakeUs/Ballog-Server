package com.example.ballog.global.config;

import com.example.ballog.global.common.exception.enums.ErrorCode;
import com.example.ballog.global.common.message.ApiErrorResponse;
import com.example.ballog.global.common.message.ApiErrorResponses;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.examples.Example;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.MediaType;
import org.springdoc.core.customizers.OperationCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.HandlerMethod;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Configuration
public class SwaggerErrorExampleConfig {

    @Bean
    public OperationCustomizer apiErrorExampleCustomizer() {
        return (Operation operation, HandlerMethod handlerMethod) -> {
            Method method = handlerMethod.getMethod();
            List<ErrorCode> errorCodes = extractErrorCodesFromAnnotations(method);

            if (!errorCodes.isEmpty()) {
                Map<Integer, ApiResponse> responseMap = new HashMap<>();

                for (ErrorCode code : errorCodes) {
                    Example example = new Example()
                            .summary(code.name())
                            .description(code.getMessage())
                            .value(toExampleJson(code));

                    responseMap.computeIfAbsent(code.getStatus(), status -> {
                        ApiResponse response = new ApiResponse().description("오류 응답");
                        MediaType mediaType = new MediaType();
                        Content content = new Content();
                        content.addMediaType("application/json", mediaType);
                        response.setContent(content);
                        return response;
                    });

                    ApiResponse response = responseMap.get(code.getStatus());
                    MediaType mediaType = response.getContent().get("application/json");
                    mediaType.addExamples(code.name(), example);
                }

                for (Map.Entry<Integer, ApiResponse> entry : responseMap.entrySet()) {
                    operation.getResponses().addApiResponse(String.valueOf(entry.getKey()), entry.getValue());
                }
            }

            return operation;
        };
    }

    private List<ErrorCode> extractErrorCodesFromAnnotations(Method method) {
        List<ErrorCode> errorCodes = new ArrayList<>();

        ApiErrorResponses container = method.getAnnotation(ApiErrorResponses.class);
        if (container != null) {
            for (ApiErrorResponse r : container.value()) {
                errorCodes.add(r.value());
            }
        } else {
            ApiErrorResponse single = method.getAnnotation(ApiErrorResponse.class);
            if (single != null) {
                errorCodes.add(single.value());
            }
        }

        return errorCodes;
    }

    private String toExampleJson(ErrorCode errorCode) {
        return String.format("""
            {
              "message": "fail",
              "status": %d,
              "error": "%s",
              "code": "%s"
            }
            """, errorCode.getStatus(), errorCode.getMessage(), errorCode.getCode());
    }
}

