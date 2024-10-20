package com.services.ConfigService.entities;


import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name="properties")
@Data
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class ConfigProperty {
    
	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	private Long id;
	
	@Column(nullable=false)
    private String application;
    
	@Column(nullable=false)
    private String value;
	
	@Column(nullable=false)
    private String profile;
    
	@Column(nullable=false)
    private String label;

    // Getters and Setters
}
