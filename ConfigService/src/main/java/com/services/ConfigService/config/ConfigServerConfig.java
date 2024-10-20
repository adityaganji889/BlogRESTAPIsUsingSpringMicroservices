package com.services.ConfigService.config;

import java.util.Collections;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.config.server.EnableConfigServer;
import org.springframework.cloud.config.server.environment.CompositeEnvironmentRepository;
import org.springframework.cloud.config.server.environment.EnvironmentRepository;
import org.springframework.cloud.config.server.environment.JdbcEnvironmentProperties;
import org.springframework.cloud.config.server.environment.JdbcEnvironmentRepository;
import org.springframework.cloud.config.server.environment.SearchPathCompositeEnvironmentRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;

import com.services.ConfigService.services.PropertyService;
import com.zaxxer.hikari.HikariDataSource;

import io.micrometer.observation.ObservationRegistry;

@Configuration
@EnableConfigServer
public class ConfigServerConfig {

    @Value("${spring.datasource.url}")
    private String datasourceUrl;

    @Value("${spring.datasource.username}")
    private String datasourceUsername;

    @Value("${spring.datasource.password}")
    private String datasourcePassword;
    
    @Autowired
    @Lazy
    private PropertyService propertyService;
    
    
    @Bean
    public DataSource dataSource() {
        HikariDataSource dataSource = new HikariDataSource();
        dataSource.setJdbcUrl(datasourceUrl);
        dataSource.setUsername(datasourceUsername);
        dataSource.setPassword(datasourcePassword);
        return dataSource;
    }
    
    @Bean
    public JdbcTemplate jdbcTemplate(DataSource dataSource) {
        return new JdbcTemplate(dataSource);
    }
    
    @Bean
    public ObservationRegistry observationRegistry() {
        return ObservationRegistry.create();
    }

    @Bean
    @Primary
    public EnvironmentRepository jdbcEnvironmentRepository(JdbcTemplate jdbcTemplate) {
//        JdbcEnvironmentProperties properties = new JdbcEnvironmentProperties();
//        // Set any custom properties if needed
//        return new CustomJdbcEnvironmentRepository(jdbcTemplate, properties, new PropertiesResultSetExtractor());
    	JdbcEnvironmentProperties properties = new JdbcEnvironmentProperties();
        CustomPropertiesResultSetExtractor extractor = new CustomPropertiesResultSetExtractor(propertyService);
        
        return new CustomJdbcEnvironmentRepository(jdbcTemplate, properties, extractor);
    }

    @Bean
    public CompositeEnvironmentRepository compositeRepository(
            JdbcEnvironmentRepository jdbcEnvironmentRepository,
            ObservationRegistry observationRegistry) {
        return new SearchPathCompositeEnvironmentRepository(
                Collections.singletonList(jdbcEnvironmentRepository),
                observationRegistry,
                true  // failOnError
        );
    }
    
}
