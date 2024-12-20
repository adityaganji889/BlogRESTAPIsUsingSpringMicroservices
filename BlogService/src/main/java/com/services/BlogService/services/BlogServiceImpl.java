package com.services.BlogService.services;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.services.BlogService.dtos.BlogRequestDTO;
import com.services.BlogService.entities.Blog;
import com.services.BlogService.repositories.BlogRepository;

@Service
public class BlogServiceImpl implements BlogService {
	
	@Autowired
	private BlogRepository blogRepository;

	@Override
	@Async("asyncTaskExecutor")
	public CompletableFuture<List<Blog>> getAllBlogs() {
		// TODO Auto-generated method stub
		return CompletableFuture.supplyAsync(() -> {
			List<Blog> blogs =  blogRepository.findAll();
			if(blogs.size()!=0) {
				return blogs;
			}
			return null;
		});
	}

	@Override
	@Async("asyncTaskExecutor")
	public CompletableFuture<List<Blog>> getAllBlogsOfAuthor(Long authorId) {
		// TODO Auto-generated method stub
	    return CompletableFuture.supplyAsync(() -> {
	    	List<Blog> blogs = blogRepository.findAllByAuthorId(authorId);
			if(blogs.size()!=0) {
				return blogs;
			}
			return null;
	    });
	}

	@Override
	@Async("asyncTaskExecutor")
	public CompletableFuture<Blog> getBlogById(String id) {
		// TODO Auto-generated method stub
		return CompletableFuture.supplyAsync(()->{
			Optional<Blog> blog = blogRepository.findById(id);
			if(blog.isPresent()) {
				return blog.get();
			}
			return null;
		});
	}

	@Override
	@Async("asyncTaskExecutor")
	public CompletableFuture<Blog> createBlog(BlogRequestDTO blogRequest,Long authorId) {
		// TODO Auto-generated method stub
		return CompletableFuture.supplyAsync(()-> {
			Blog blog = new Blog();
			blog.setTitle(blogRequest.getTitle());
			blog.setContent(blogRequest.getContent());
			blog.setAuthorId(authorId);
			Blog savedBlog = blogRepository.save(blog);
			return savedBlog;
		});
	}

	@Override
	@Async("asyncTaskExecutor")
	public CompletableFuture<Blog> updateBlog(BlogRequestDTO blogRequest, String id) {
		// TODO Auto-generated method stub
		return CompletableFuture.supplyAsync(()->{
			Optional<Blog> blog = blogRepository.findById(id);
			if(blog.isPresent()) {
				blog.get().setTitle(blogRequest.getTitle());
				blog.get().setContent(blogRequest.getContent());
				Blog savedBlog = blogRepository.save(blog.get());
				return savedBlog;	
			}
			return null;
		});
	}

	@Override
	@Async("asyncTaskExecutor")
	public CompletableFuture<String> deleteBlog(String id) {
		// TODO Auto-generated method stub
		return CompletableFuture.supplyAsync(()->{
			Optional<Blog> blog = blogRepository.findById(id);
			if(blog.isPresent()) {
				blogRepository.delete(blog.get());
				return "Blog is deleted successfully.";	
			}
			return null;
		});
	}

	@Override
	@Async("asyncTaskExecutor")
	public CompletableFuture<String> deleteAllBlogsOfDeletedUsers(Long id) {
		// TODO Auto-generated method stub
		return CompletableFuture.supplyAsync(()->{
			CompletableFuture<List<Blog>> blogs = getAllBlogsOfAuthor(id);
			List<Blog> blogsList = null;
			try {
				blogsList = blogs.get();
			}
			catch(Exception e) {
				e.printStackTrace();
			}
			if(blogsList!=null) {
				for (Blog blog : blogsList) {
					blogRepository.delete(blog);
				}
			}
			return "All blogs of the deleted user deleted successfully.";
		});
	}

}
