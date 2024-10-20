package com.services.BlogService.services;

import java.util.List;

import com.services.BlogService.dtos.BlogRequestDTO;
import com.services.BlogService.entities.Blog;

public interface BlogService {

    List<Blog> getAllBlogs();
    
    List<Blog> getAllBlogsOfAuthor(Long authorId);
    
    Blog getBlogById(String id);
    
    Blog createBlog(BlogRequestDTO blogRequest, Long authorId);
    
    Blog updateBlog(BlogRequestDTO blogRequest, String id);
    
    String deleteBlog(String id);
    
    String deleteAllBlogsOfDeletedUsers(Long id);
	
}
