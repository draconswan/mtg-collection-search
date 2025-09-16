package com.dswan.mtg;

import jakarta.annotation.PostConstruct;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.jdbc.autoconfigure.DataSourceAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

import java.sql.DriverManager;
import java.sql.SQLException;

@Configuration
@SpringBootApplication
@EnableConfigurationProperties
@EnableJpaRepositories(basePackages= "com.dswan.mtg.repository")
@ComponentScan(basePackages = "com.dswan.mtg")
public class MtgCollectionSearchApplication {

    public static void main(String[] args) {
        SpringApplication.run(MtgCollectionSearchApplication.class, args);
    }

    @PostConstruct
    public void logDriver() throws SQLException {
        System.out.println("JDBC Driver: " + DriverManager.getDriver("jdbc:postgresql://localhost:5432/mtg-database"));
    }
}