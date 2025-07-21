package com.example.ballog.global.config;

import javax.sql.DataSource;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class SshDataSourceConfig {
    private final SshTunnelConfig initializer;

    @Bean("dataSource")
    @Primary
    public DataSource dataSource(DataSourceProperties properties) {
        log.info("Datasource Properties URL: {}", properties.getUrl());
        log.info("Datasource Properties Username: {}", properties.getUsername());
        log.info("Datasource Properties Password: {}", properties.getPassword());
        log.info("Datasource Properties Driver ClassName: {}", properties.getDriverClassName());
        Integer forwardedPort = initializer.buildSshConnection();
        String url = properties.getUrl().replace("[forwardedPort]", String.valueOf(forwardedPort));
        log.info(url);
        return DataSourceBuilder.create()
                .url(url)
                .username(properties.getUsername())
                .password(properties.getPassword())
                .driverClassName(properties.getDriverClassName())
                .build();
    }
}
