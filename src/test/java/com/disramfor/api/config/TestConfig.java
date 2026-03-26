package com.disramfor.api.config;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.testcontainers.containers.MySQLContainer;

@TestConfiguration
@Profile("integration-test")
public class TestConfig {

    @Bean
    public PasswordEncoder testPasswordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public MySQLContainer<?> mysqlContainer() {
        @SuppressWarnings("resource")
        MySQLContainer<?> mysql = new MySQLContainer<>("mysql:8.0")
                .withDatabaseName("disramfor_test")
                .withUsername("test")
                .withPassword("test")
                .withReuse(true);

        mysql.start();

        // Configurar las propiedades de conexión dinámicamente
        System.setProperty("spring.datasource.url", mysql.getJdbcUrl());
        System.setProperty("spring.datasource.username", mysql.getUsername());
        System.setProperty("spring.datasource.password", mysql.getPassword());

        return mysql;
    }
}