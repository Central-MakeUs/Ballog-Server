package com.example.ballog.global.config;

import com.example.ballog.domain.login.security.CustomUserDetailsService;
import com.example.ballog.domain.login.security.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

import static org.springframework.security.config.Customizer.withDefaults;

@Configuration
@EnableWebSecurity(debug = true)
@RequiredArgsConstructor
public class SecurityConfig {

    private final CustomUserDetailsService customUserDetailsService;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrfConfig -> csrfConfig.disable())
                .headers(headerConfig -> headerConfig.frameOptions(frameOptionsConfig -> frameOptionsConfig.disable()))
                .authorizeHttpRequests(authorizeRequests -> authorizeRequests
                        .requestMatchers(org.springframework.http.HttpMethod.OPTIONS, "/**").permitAll() //OPTIONS 메서드는 항상 허용
                        .requestMatchers(URL_TO_PERMIT).permitAll()
                        .anyRequest().authenticated()
                )

                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .cors(cors -> cors.disable())
                .addFilterBefore(new JwtAuthenticationFilter(customUserDetailsService), UsernamePasswordAuthenticationFilter.class);


        return http.build();
    }

    private static final String[] URL_TO_PERMIT = {
            "/v3/api-docs/**",
            "/swagger-ui/**",
            "/swagger-resources/**",
            "/error",
            "/api/v1/**",
            "/favicon.ico",
            "https://appleid.apple.com/**"
    };



    @Bean
    public BCryptPasswordEncoder bCryptPasswordEncoder() {
        return new BCryptPasswordEncoder();
    }

}
