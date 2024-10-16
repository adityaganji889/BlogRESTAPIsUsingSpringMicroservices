package com.services.ConfigService.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.*;

import com.services.ConfigService.entities.ConfigProperty;


public interface PropertyRepository extends JpaRepository<ConfigProperty,Long> {

	List<ConfigProperty> findByProfileAndLabel(String profile, String label);
	
}
