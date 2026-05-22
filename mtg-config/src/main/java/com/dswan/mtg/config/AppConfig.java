package com.dswan.mtg.config;

import liquibase.integration.spring.SpringLiquibase;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestTemplate;
import tools.jackson.databind.DeserializationFeature;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.PropertyNamingStrategies;
import tools.jackson.databind.json.JsonMapper;
import org.thymeleaf.extras.springsecurity6.dialect.SpringSecurityDialect;

import javax.sql.DataSource;

@Configuration
@EnableRetry
public class AppConfig {
    @Bean
    @Profile("!test")
    public SpringLiquibase liquibase(DataSource dataSource) {
        SpringLiquibase liquibase = new SpringLiquibase();
        liquibase.setDataSource(dataSource);
        liquibase.setChangeLog("classpath:db/db.changelog-master.yaml");
        liquibase.setDefaultSchema("public");
        liquibase.setShouldRun(true);  // force execution
        return liquibase;
    }

    @Bean
    public RestClient restClient() {
        return RestClient.builder()
                .requestFactory(new HttpComponentsClientHttpRequestFactory())
                .defaultHeader("User-Agent", "MTGCollectionSearch/1.0.0 (https://github.com/draconswan/mtg-collection-search)")
                .defaultHeader("Accept", "application/json")
                .build();
    }

    @Bean
    public ObjectMapper jsonObjectMapper() {
        return JsonMapper.builder()
                .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
                .disable(DeserializationFeature.FAIL_ON_NULL_FOR_PRIMITIVES)
                .propertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE)
                .build();
    }

    @Bean
    public SpringSecurityDialect springSecurityDialect() {
        return new SpringSecurityDialect();
    }
}
