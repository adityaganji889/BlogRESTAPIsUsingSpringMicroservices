package com.services.ConfigService.config;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.springframework.jdbc.core.ResultSetExtractor;

import com.services.ConfigService.services.PropertyService;

public class CustomPropertiesResultSetExtractor implements ResultSetExtractor<List<Property>> {
    
    private final PropertyService propertyService;

    public CustomPropertiesResultSetExtractor(PropertyService propertyService) {
        this.propertyService = propertyService;
    }

    @Override
    public List<Property> extractData(ResultSet rs) throws SQLException {
        List<Property> properties = new ArrayList<>();
        while (rs.next()) {
            // Construct Property object based on your DB schema
            Property property = new Property();
//            property.setKey(rs.getString("application"));
            String arr[] = rs.getString("value").split("=");
            property.setKey(arr[0]);
            property.setValue(arr[1]);
            // Set other fields as necessary
            properties.add(property);
        }
        return properties;
    }
}

