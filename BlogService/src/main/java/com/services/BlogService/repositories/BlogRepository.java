package com.services.BlogService.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.services.BlogService.entities.Blog;

public interface BlogRepository extends MongoRepository<Blog, String> {
    // Custom query methods can be added here
	Optional<Blog> findById(String Id);
	
	List<Blog> findAllByAuthorId(Long authorId);
}
