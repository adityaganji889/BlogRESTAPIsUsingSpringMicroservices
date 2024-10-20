package com.services.ConfigService.config;

import java.util.HashMap;
import java.util.List;

import org.springframework.cloud.config.environment.Environment;
import org.springframework.cloud.config.environment.PropertySource;
import org.springframework.cloud.config.server.environment.JdbcEnvironmentProperties;
import org.springframework.cloud.config.server.environment.JdbcEnvironmentRepository;
import org.springframework.jdbc.core.JdbcTemplate;

public class CustomJdbcEnvironmentRepository extends JdbcEnvironmentRepository {

	private final CustomPropertiesResultSetExtractor extractor;

	private final JdbcTemplate jdbcTemplate;

	public CustomJdbcEnvironmentRepository(JdbcTemplate jdbcTemplate, JdbcEnvironmentProperties properties,
			CustomPropertiesResultSetExtractor extractor) {
		super(jdbcTemplate, properties);
		this.extractor = extractor;
		this.jdbcTemplate = jdbcTemplate;
	}

	@Override
	public Environment findOne(String application, String profile, String label) {
		 if (profile == null) {
		        profile = "default"; // Fallback to default profile
		    }

		    // Fetch properties as key=value strings
		    List<Property> properties = jdbcTemplate.query(
		            "SELECT `application`,`value` FROM properties WHERE `application` = ? AND `profile` = ? AND `label` = ?",
		            extractor, application, profile, label
		    );

		    // Create a map to hold the properties
		    HashMap<String, Object> propertyMap = new HashMap<>();

		 // Populate the property map with all properties
		    for (Property property : properties) {
		        propertyMap.put(property.getKey(), property.getValue());
		    }

		    // Create a PropertySource using the application name as the source name
		    PropertySource propertySource = new PropertySource(application, propertyMap);

		    // Create the environment with the application, profile, and label
		    Environment environment = new Environment(application, profile, label);

		    // Add the PropertySource to the environment
		    environment.add(propertySource);
		    
		    return environment;
	}
}
