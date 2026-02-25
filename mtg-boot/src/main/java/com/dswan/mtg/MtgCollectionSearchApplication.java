package com.dswan.mtg;

import com.dswan.mtg.config.MTGProperties;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@Configuration
@SpringBootApplication
@EnableConfigurationProperties(MTGProperties.class)
@EnableJpaRepositories(basePackages = "com.dswan.mtg.repository")
@ComponentScan(basePackages = "com.dswan.mtg")
public class MtgCollectionSearchApplication {

    @Autowired
    ConfigurableEnvironment env;

    public static void main(String[] args) {
        SpringApplication.run(MtgCollectionSearchApplication.class, args);
    }

    @PostConstruct
    public void logDriver() {
//        System.out.println("=== ALL PROPERTIES IN ENVIRONMENT ===");
//        for (org.springframework.core.env.PropertySource<?> ps : env.getPropertySources()) {
//            System.out.println("---- Source: " + ps.getName() + " ----");
//
//            if (ps instanceof org.springframework.core.env.MapPropertySource mps) {
//                for (String key : mps.getPropertyNames()) {
//                    System.out.println(key + " = " + mps.getProperty(key));
//                }
//            }
//        }
//        env.getPropertySources().forEach(ps -> System.out.println("SOURCE: " + ps.getName()));
    }
}