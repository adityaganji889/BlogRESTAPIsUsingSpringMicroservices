package com.services.BlogService.services;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.services.BlogService.dtos.BlogRequestDTO;
import com.services.BlogService.entities.Blog;
import com.services.BlogService.repositories.BlogRepository;

@Service
public class BlogServiceImpl implements BlogService {
	
	@Autowired
	private BlogRepository blogRepository;

	@Override
	public List<Blog> getAllBlogs() {
		// TODO Auto-generated method stub
		List<Blog> blogs =  blogRepository.findAll();
		if(blogs.size()!=0) {
			return blogs;
		}
		return null;
	}

	@Override
	public List<Blog> getAllBlogsOfAuthor(Long authorId) {
		// TODO Auto-generated method stub
	    List<Blog> blogs = blogRepository.findAllByAuthorId(authorId);
		if(blogs.size()!=0) {
			return blogs;
		}
		return null;
	}

	@Override
	public Blog getBlogById(String id) {
		// TODO Auto-generated method stub
		Optional<Blog> blog = blogRepository.findById(id);
		if(blog.isPresent()) {
			return blog.get();
		}
		return null;
	}

	@Override
	public Blog createBlog(BlogRequestDTO blogRequest,Long authorId) {
		// TODO Auto-generated method stub
		Blog blog = new Blog();
		blog.setTitle(blogRequest.getTitle());
		blog.setContent(blogRequest.getContent());
		blog.setAuthorId(authorId);
		Blog savedBlog = blogRepository.save(blog);
		return savedBlog;
	}

	@Override
	public Blog updateBlog(BlogRequestDTO blogRequest, String id) {
		// TODO Auto-generated method stub
		Optional<Blog> blog = blogRepository.findById(id);
		if(blog.isPresent()) {
			blog.get().setTitle(blogRequest.getTitle());
			blog.get().setContent(blogRequest.getContent());
			Blog savedBlog = blogRepository.save(blog.get());
			return savedBlog;	
		}
		return null;
	}

	@Override
	public String deleteBlog(String id) {
		// TODO Auto-generated method stub
		Optional<Blog> blog = blogRepository.findById(id);
		if(blog.isPresent()) {
			blogRepository.delete(blog.get());
			return "Blog is deleted successfully.";	
		}
		return null;
	}

	@Override
	public String deleteAllBlogsOfDeletedUsers(Long id) {
		// TODO Auto-generated method stub
		List<Blog> blogs = getAllBlogsOfAuthor(id);
		if(blogs!=null) {
			for (Blog blog : blogs) {
				blogRepository.delete(blog);
			}
		}
		return "All blogs of the deleted user deleted successfully.";
	}

}
