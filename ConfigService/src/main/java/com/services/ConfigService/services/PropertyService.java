package com.services.ConfigService.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.services.ConfigService.entities.ConfigProperty;
import com.services.ConfigService.repositories.PropertyRepository;

import java.util.List;

@Service
public class PropertyService {

    @Autowired
    private PropertyRepository propertyRepository;

    // Fetch all properties for the given application, profile, and label
    public List<ConfigProperty> getAllProperties(String application, String profile, String label) {
        return propertyRepository.findByProfileAndLabel(profile, label);
    }
}

