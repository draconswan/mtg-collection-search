package com.dswan.mtg;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@Configuration
@SpringBootApplication
@EnableConfigurationProperties
@EnableJpaRepositories(basePackages= "com.dswan.mtg.repository")
@ComponentScan(basePackages = "com.dswan.mtg")
public class MtgCollectionSearchApplication {

    public static void main(String[] args) {
        SpringApplication.run(MtgCollectionSearchApplication.class, args);
    }

}