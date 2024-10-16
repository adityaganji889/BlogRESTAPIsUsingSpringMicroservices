package com.services.BlogService.controllers;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import com.services.BlogService.dtos.BlogAuthor;
import com.services.BlogService.dtos.BlogPopulatedResponse;
import com.services.BlogService.dtos.BlogRequestDTO;
import com.services.BlogService.dtos.BlogResponse;
import com.services.BlogService.dtos.BlogResponseDTO;
import com.services.BlogService.dtos.BlogsListResponseDTO;
import com.services.BlogService.dtos.BlogsOfAuthorListResponseDTO;
import com.services.BlogService.dtos.Role;
//import com.services.BlogService.dtos.UserInfo;
import com.services.BlogService.dtos.UserInfoResponse;
import com.services.BlogService.entities.Blog;
import com.services.BlogService.services.BlogService;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;


@RestController
@RequestMapping("/api/blogs")
@CrossOrigin("*")
@SecurityRequirement(name="BearerAuth")
@Tag(name="Blog APIs")
public class BlogController {

	@Autowired
	private BlogService blogService;

	@Autowired
	private RestTemplate restTemplate;

	public HttpEntity<String> authorizeHeaders(String authorizedHeader) {
		HttpHeaders headers = new HttpHeaders();
		headers.set("Authorization", authorizedHeader);
		HttpEntity<String> entity = new HttpEntity<>(headers);
		return entity;
	}

	public UserInfoResponse getLoggedInUserInfo(HttpEntity<String> entity) {
		UserInfoResponse userInfoResponse = new UserInfoResponse();
		try {
			userInfoResponse = restTemplate.exchange("http://USER-SERVICE/api/userProfile/userDetails", HttpMethod.GET,
					entity, UserInfoResponse.class).getBody();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return userInfoResponse;
	}
	
	public UserInfoResponse getUserInfoByUserId(HttpEntity<String> entity, Long id) {
		UserInfoResponse userInfoResponse = new UserInfoResponse();
		try {
			userInfoResponse = restTemplate.exchange("http://USER-SERVICE/api/userProfile/userDetails/"+id, HttpMethod.GET,
					entity, UserInfoResponse.class).getBody();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return userInfoResponse;
	}

	@GetMapping("/getAllBlogs")
	public ResponseEntity<BlogsListResponseDTO> getAllBlogs(
			@RequestHeader(value=HttpHeaders.AUTHORIZATION, required=false) String authorizationHeader) {
		HttpEntity<String> entity = authorizeHeaders(authorizationHeader);
//		UserInfoResponse userInfo = getLoggedInUserInfo(entity);
		List<Blog> blogs = blogService.getAllBlogs();
		BlogsListResponseDTO blogsResponse = new BlogsListResponseDTO();
		List<BlogPopulatedResponse> populatedBlogs = new ArrayList<BlogPopulatedResponse>();
		if (blogs != null) {
			for (int i = 0; i < blogs.size(); i++) {
				BlogPopulatedResponse populatedBlog = new BlogPopulatedResponse();
				UserInfoResponse userInfo1 = getUserInfoByUserId(entity,blogs.get(i).getAuthorId());
				BlogAuthor author = new BlogAuthor();
				author.setId(blogs.get(i).getAuthorId());
				author.setUsername(userInfo1.getUserInfo().getUsername());
				author.setRole(userInfo1.getUserInfo().getRole());
				populatedBlog.setId(blogs.get(i).getId());
				populatedBlog.setTitle(blogs.get(i).getTitle());
				populatedBlog.setContent(blogs.get(i).getContent());
				populatedBlog.setAuthor(author);
				populatedBlogs.add(populatedBlog);
			}
			blogsResponse.setSuccess(true);
			blogsResponse.setMessage("All blogs fetched successfully.");
			blogsResponse.setBlogs(populatedBlogs);
			return new ResponseEntity<BlogsListResponseDTO>(blogsResponse, HttpStatus.OK);
		} else {
			blogsResponse.setSuccess(true);
			blogsResponse.setMessage("No blogs to display.");
			blogsResponse.setBlogs(null);
			return new ResponseEntity<BlogsListResponseDTO>(blogsResponse, HttpStatus.NOT_FOUND);
		}
	}

	@GetMapping("/getAllBlogs/{authorId}")
	public ResponseEntity<BlogsOfAuthorListResponseDTO> getAllBlogsOfAuthor(
			@RequestHeader(value=HttpHeaders.AUTHORIZATION, required=false) String authorizationHeader, @PathVariable("authorId") Long authorId) {
		HttpEntity<String> entity = authorizeHeaders(authorizationHeader);
		UserInfoResponse userInfo = getLoggedInUserInfo(entity);
		List<Blog> blogs = blogService.getAllBlogsOfAuthor(authorId);
		BlogsOfAuthorListResponseDTO blogsResponse = new BlogsOfAuthorListResponseDTO();
		List<BlogResponse> populatedBlogs = new ArrayList<BlogResponse>();
		if (blogs != null) {
			UserInfoResponse userInfo1 = getUserInfoByUserId(entity,authorId);
			BlogAuthor author = new BlogAuthor();
			author.setId(authorId);
			author.setUsername(userInfo1.getUserInfo().getUsername());
			author.setRole(userInfo1.getUserInfo().getRole());
			for (int i = 0; i < blogs.size(); i++) {
				BlogResponse populatedBlog = new BlogResponse();
				populatedBlog.setId(blogs.get(i).getId());
				populatedBlog.setTitle(blogs.get(i).getTitle());
				populatedBlog.setContent(blogs.get(i).getContent());
				populatedBlogs.add(populatedBlog);
			}
			blogsResponse.setSuccess(true);
			blogsResponse.setMessage("All blogs fetched successfully.");
			blogsResponse.setAuthor(author);
			blogsResponse.setBlogs(populatedBlogs);
			return new ResponseEntity<BlogsOfAuthorListResponseDTO>(blogsResponse, HttpStatus.OK);
		} else {
			blogsResponse.setSuccess(true);
			blogsResponse.setMessage("No blogs of this author found to display.");
			blogsResponse.setBlogs(null);
			blogsResponse.setBlogs(populatedBlogs);
			return new ResponseEntity<BlogsOfAuthorListResponseDTO>(blogsResponse, HttpStatus.NOT_FOUND);
		}
	}

	@PostMapping("/createNewBlog")
	public ResponseEntity<BlogResponseDTO> createNewBlog(@RequestHeader(value=HttpHeaders.AUTHORIZATION, required=false) String authorizationHeader,
			@RequestBody BlogRequestDTO blogRequest) throws Exception {
		HttpEntity<String> entity = authorizeHeaders(authorizationHeader);
		UserInfoResponse author = getLoggedInUserInfo(entity);
		Blog blog = blogService.createBlog(blogRequest, author.getUserInfo().getId());
		BlogPopulatedResponse blogsPopulatedResponse = new BlogPopulatedResponse();
		BlogResponseDTO blogResponse = new BlogResponseDTO();
		if (blog != null) {
			BlogAuthor blogAuthor = new BlogAuthor();
			blogAuthor.setId(author.getUserInfo().getId());
			blogAuthor.setUsername(author.getUserInfo().getUsername());
			blogAuthor.setRole(author.getUserInfo().getRole());
			blogsPopulatedResponse.setId(blog.getId());
			blogsPopulatedResponse.setTitle(blog.getTitle());
			blogsPopulatedResponse.setContent(blog.getContent());
			blogsPopulatedResponse.setAuthor(blogAuthor);
			blogResponse.setSuccess(true);
			blogResponse.setMessage("New blog created successfully.");
			blogResponse.setBlog(blogsPopulatedResponse);
			return new ResponseEntity<BlogResponseDTO>(blogResponse, HttpStatus.OK);
		} else {
			blogResponse.setSuccess(false);
			blogResponse.setMessage("Error in creating a new blog.");
			blogResponse.setBlog(null);
			return new ResponseEntity<BlogResponseDTO>(blogResponse, HttpStatus.BAD_REQUEST);
		}
	}

	@PutMapping("/updateExistingBlog/{id}")
	public ResponseEntity<BlogResponseDTO> updateBlog(@RequestHeader(value=HttpHeaders.AUTHORIZATION, required=false) String authorizationHeader,
			@PathVariable("id") String id, @RequestBody BlogRequestDTO blogRequest) {
		HttpEntity<String> entity = authorizeHeaders(authorizationHeader);
		UserInfoResponse author = getLoggedInUserInfo(entity);

		Blog blog = blogService.getBlogById(id);
		BlogPopulatedResponse blogsPopulatedResponse = new BlogPopulatedResponse();
		BlogResponseDTO blogResponse = new BlogResponseDTO();
		if (blog != null) {
			BlogAuthor blogAuthor = new BlogAuthor();
			if ((blog.getAuthorId().equals(author.getUserInfo().getId()))||(author.getUserInfo().getRole().equals(Role.ADMIN))) {
				Blog updatedBlog = blogService.updateBlog(blogRequest, id);
				if((author.getUserInfo().getRole().equals(Role.ADMIN))&&(!blog.getAuthorId().equals(author.getUserInfo().getId()))) {
					UserInfoResponse originalAuthor = getUserInfoByUserId(entity,blog.getAuthorId());
					blogAuthor.setId(originalAuthor.getUserInfo().getId());
					blogAuthor.setUsername(originalAuthor.getUserInfo().getUsername());
					blogAuthor.setRole(originalAuthor.getUserInfo().getRole());
				}
				else {
					blogAuthor.setId(author.getUserInfo().getId());
					blogAuthor.setUsername(author.getUserInfo().getUsername());
					blogAuthor.setRole(author.getUserInfo().getRole());
				}
				blogsPopulatedResponse.setId(updatedBlog.getId());
				blogsPopulatedResponse.setTitle(updatedBlog.getTitle());
				blogsPopulatedResponse.setContent(updatedBlog.getContent());
				blogsPopulatedResponse.setAuthor(blogAuthor);
				blogResponse.setSuccess(true);
				blogResponse.setMessage("Blog with id:" + id + " updated successfully.");
				blogResponse.setBlog(blogsPopulatedResponse);
				return new ResponseEntity<BlogResponseDTO>(blogResponse, HttpStatus.OK);
			} else {
				blogResponse.setSuccess(false);
				blogResponse.setMessage("Error while updating this blog. You're trying to update someone else's blog.");
				blogResponse.setBlog(null);
				return new ResponseEntity<BlogResponseDTO>(blogResponse, HttpStatus.FORBIDDEN);
			}
		} else {
			blogResponse.setSuccess(false);
			blogResponse.setMessage("Error while updating this blog.");
			blogResponse.setBlog(null);
			return new ResponseEntity<BlogResponseDTO>(blogResponse, HttpStatus.BAD_REQUEST);
		}
	}

	@DeleteMapping("/deleteExistingBlog/{id}")
	public ResponseEntity<BlogResponseDTO> deleteBlog(@RequestHeader(value=HttpHeaders.AUTHORIZATION, required=false) String authorizationHeader,
			@PathVariable("id") String id) {
		HttpEntity<String> entity = authorizeHeaders(authorizationHeader);
		UserInfoResponse author = getLoggedInUserInfo(entity);
		Blog blog = blogService.getBlogById(id);
		BlogResponseDTO blogResponse = new BlogResponseDTO();
		if (blog != null) {
			if ((blog.getAuthorId().equals(author.getUserInfo().getId()))||(author.getUserInfo().getRole().equals(Role.ADMIN))) {
				String deletedBlog = blogService.deleteBlog(id);
				if (deletedBlog != null) {
					blogResponse.setSuccess(true);
					blogResponse.setMessage("Blog with id:" + id + " deleted successfully.");
					blogResponse.setBlog(null);
					return new ResponseEntity<BlogResponseDTO>(blogResponse, HttpStatus.OK);
				} else {
					blogResponse.setSuccess(false);
					blogResponse.setMessage("Blog not found to delete.");
					blogResponse.setBlog(null);
					return new ResponseEntity<BlogResponseDTO>(blogResponse, HttpStatus.NOT_FOUND);
				}

			} else {
				blogResponse.setSuccess(false);
				blogResponse.setMessage("Error while deleting this blog. You're trying to update someone else's blog.");
				blogResponse.setBlog(null);
				return new ResponseEntity<BlogResponseDTO>(blogResponse, HttpStatus.FORBIDDEN);
			}
		} else {
			blogResponse.setSuccess(false);
			blogResponse.setMessage("Blog not found to delete.");
			blogResponse.setBlog(null);
			return new ResponseEntity<BlogResponseDTO>(blogResponse, HttpStatus.NOT_FOUND);
		}
	}
}
