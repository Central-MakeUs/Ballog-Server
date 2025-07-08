package com.example.ballog.global.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springdoc.core.customizers.OperationCustomizer;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {
    @Bean
    public GroupedOpenApi jwtApi(@Qualifier("apiErrorExampleCustomizer")OperationCustomizer apiErrorExampleCustomizer){
        return GroupedOpenApi.builder()
                .group("jwt-api")
                .pathsToMatch("/**")
                .addOperationCustomizer(apiErrorExampleCustomizer)
                .build();
    }

    @Bean
    public OpenAPI customOpenAPI(){
        return new OpenAPI()
                .components(new Components())
                .info(new Info().title("Ballog API")
                        .description("Ballog API 입니다")
                        .version("v1.0.0"));
    }

}
