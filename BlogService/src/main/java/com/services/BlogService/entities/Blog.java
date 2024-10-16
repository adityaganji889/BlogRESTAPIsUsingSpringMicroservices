package com.services.BlogService.entities;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.Data;

@Document(collection="blogs")
@Data
public class Blog {
	
    @Id
    private String id;
    
    private String title;
    
    private String content;
    
    private Long authorId; // Reference to User ID

    // Getters and Setters
}
