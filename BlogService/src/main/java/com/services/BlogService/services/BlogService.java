package com.services.BlogService.services;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import com.services.BlogService.dtos.BlogRequestDTO;
import com.services.BlogService.entities.Blog;

public interface BlogService {

    CompletableFuture<List<Blog>> getAllBlogs();
    
    CompletableFuture<List<Blog>> getAllBlogsOfAuthor(Long authorId);
    
    CompletableFuture<Blog> getBlogById(String id);
    
    CompletableFuture<Blog> createBlog(BlogRequestDTO blogRequest, Long authorId);
    
    CompletableFuture<Blog> updateBlog(BlogRequestDTO blogRequest, String id);
    
    CompletableFuture<String> deleteBlog(String id);
    
    CompletableFuture<String> deleteAllBlogsOfDeletedUsers(Long id);
	
}
