package com.services.BlogService.controllers;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

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
import com.services.BlogService.dtos.DefaultResponse;
import com.services.BlogService.dtos.Role;
//import com.services.BlogService.dtos.UserInfo;
import com.services.BlogService.dtos.UserInfoResponse;
import com.services.BlogService.entities.Blog;
import com.services.BlogService.services.BlogService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/blogs")
@CrossOrigin("*")
@SecurityRequirement(name = "BearerAuth")
@Tag(name = "Blog APIs", description = "APIs for managing blog entries, including creating, updating, fetching, and deleting blogs.")
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

	public CompletableFuture<UserInfoResponse> getLoggedInUserInfo(HttpEntity<String> entity) {
		return CompletableFuture.supplyAsync(() -> {
			UserInfoResponse userInfoResponse = new UserInfoResponse();
			try {
				userInfoResponse = restTemplate.exchange("http://USER-SERVICE/api/userProfile/userDetails", HttpMethod.GET,
						entity, UserInfoResponse.class).getBody();
			} catch (Exception e) {
				e.printStackTrace();
			}
			return userInfoResponse;
		});
	}

	public CompletableFuture<UserInfoResponse> getUserInfoByUserId(HttpEntity<String> entity, Long id) {
		return CompletableFuture.supplyAsync(() -> {
			UserInfoResponse userInfoResponse = new UserInfoResponse();
			try {
				userInfoResponse = restTemplate.exchange("http://USER-SERVICE/api/userProfile/userDetails/" + id,
						HttpMethod.GET, entity, UserInfoResponse.class).getBody();
			} catch (Exception e) {
				e.printStackTrace();
			}
			return userInfoResponse;
		});
	}

	@Operation(summary = "Get all blogs", description = "Fetches a list of all blogs.")
	@ApiResponses(value = {
		@ApiResponse(responseCode = "200", description = "All blogs fetched successfully", 
			content = @Content(schema = @Schema(implementation = BlogsListResponseDTO.class))),
		@ApiResponse(responseCode = "404", description = "No blogs to display", content = @Content)
	})
	@GetMapping("/getAllBlogs")
	public ResponseEntity<BlogsListResponseDTO> getAllBlogs(
			@RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) @Parameter(hidden = true) String authorizationHeader) {
		HttpEntity<String> entity = authorizeHeaders(authorizationHeader);
		CompletableFuture<List<Blog>> blogsList = blogService.getAllBlogs();
		List<Blog> blogs = null;
		try {
			blogs = blogsList.get();
		}
		catch(Exception e) {
			e.printStackTrace();
		}
		BlogsListResponseDTO blogsResponse = new BlogsListResponseDTO();
		List<BlogPopulatedResponse> populatedBlogs = new ArrayList<BlogPopulatedResponse>();
		if (blogs != null) {
			for (int i = 0; i < blogs.size(); i++) {
				BlogPopulatedResponse populatedBlog = new BlogPopulatedResponse();
				CompletableFuture<UserInfoResponse> cfu = getUserInfoByUserId(entity, blogs.get(i).getAuthorId());
				UserInfoResponse userInfo1 = null;
				try {
					userInfo1 = cfu.get();
				}
				catch(Exception e) {
					e.printStackTrace();
				}
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

	@Operation(summary = "Get all blogs of an author", description = "Fetches a list of blogs written by a specific author.")
	@ApiResponses(value = {
		@ApiResponse(responseCode = "200", description = "All blogs fetched successfully", 
			content = @Content(schema = @Schema(implementation = BlogsOfAuthorListResponseDTO.class))),
		@ApiResponse(responseCode = "404", description = "No blogs of this author found", content = @Content)
	})
	@GetMapping("/getAllBlogs/{authorId}")
	public ResponseEntity<BlogsOfAuthorListResponseDTO> getAllBlogsOfAuthor(
			@RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) @Parameter(hidden = true) String authorizationHeader,
			@PathVariable("authorId") Long authorId) {
		HttpEntity<String> entity = authorizeHeaders(authorizationHeader);
		CompletableFuture<List<Blog>> blogsList = blogService.getAllBlogsOfAuthor(authorId);
		List<Blog> blogs = null;
		try {
			blogs = blogsList.get();
		}
		catch(Exception e) {
			e.printStackTrace();
		}
		BlogsOfAuthorListResponseDTO blogsResponse = new BlogsOfAuthorListResponseDTO();
		List<BlogResponse> populatedBlogs = new ArrayList<BlogResponse>();
		if (blogs != null) {
			CompletableFuture<UserInfoResponse> cfu = getUserInfoByUserId(entity, authorId);
			UserInfoResponse userInfo1 = null;
			try {
				userInfo1 = cfu.get();
			}
			catch(Exception e) {
				e.printStackTrace();
			}
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

	@Operation(summary = "Create a new blog", description = "Creates a new blog entry.")
	@ApiResponses(value = {
		@ApiResponse(responseCode = "200", description = "New blog created successfully", 
			content = @Content(schema = @Schema(implementation = BlogResponseDTO.class))),
		@ApiResponse(responseCode = "400", description = "Error in creating a new blog", content = @Content)
	})
	@PostMapping("/createNewBlog")
	public ResponseEntity<BlogResponseDTO> createNewBlog(
			@RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) @Parameter(hidden = true) String authorizationHeader,
			@RequestBody BlogRequestDTO blogRequest) throws Exception {
		HttpEntity<String> entity = authorizeHeaders(authorizationHeader);
		CompletableFuture<UserInfoResponse> user = getLoggedInUserInfo(entity);
		UserInfoResponse author = null;
		try {
			author = user.get();
		}
		catch(Exception e) {
			e.printStackTrace();
		}
		CompletableFuture<Blog> blog1 = blogService.createBlog(blogRequest, author.getUserInfo().getId());
		Blog blog = null;
		try {
			blog = blog1.get();
		}
		catch(Exception e) {
			e.printStackTrace();
		}
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

	@Operation(summary = "Get a blog by ID", description = "Fetches a blog entry by its ID.")
	@ApiResponses(value = {
		@ApiResponse(responseCode = "200", description = "Blog fetched successfully", 
			content = @Content(schema = @Schema(implementation = BlogResponseDTO.class))),
		@ApiResponse(responseCode = "404", description = "Blog not found", content = @Content)
	})
	@GetMapping("/getBlog/{id}")
	public ResponseEntity<BlogResponseDTO> getBlog(
			@RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) @Parameter(hidden = true) String authorizationHeader,
			@PathVariable("id") String id) {
		HttpEntity<String> entity = authorizeHeaders(authorizationHeader);
		CompletableFuture<UserInfoResponse> user = getLoggedInUserInfo(entity);
		UserInfoResponse author = null;
		try {
			author = user.get();
		}
		catch(Exception e) {
			e.printStackTrace();
		}
		CompletableFuture<Blog> blog1 = blogService.getBlogById(id);
		Blog blog = null;
		try {
			blog = blog1.get();
		}
		catch(Exception e) {
			e.printStackTrace();
		}
//		UserInfoResponse author = getLoggedInUserInfo(entity);
//		Blog blog = blogService.getBlogById(id);
		BlogPopulatedResponse blogsPopulatedResponse = new BlogPopulatedResponse();
		BlogResponseDTO blogResponse = new BlogResponseDTO();
		if (blog != null) {
			BlogAuthor blogAuthor = new BlogAuthor();
			CompletableFuture<UserInfoResponse> originalAuthor1 = getUserInfoByUserId(entity, blog.getAuthorId());
			UserInfoResponse originalAuthor = null;
			try {
				originalAuthor = originalAuthor1.get();
			}
			catch(Exception e) {
				e.printStackTrace();
			}
			blogAuthor.setId(originalAuthor.getUserInfo().getId());
			blogAuthor.setUsername(originalAuthor.getUserInfo().getUsername());
			blogAuthor.setRole(originalAuthor.getUserInfo().getRole());
			blogsPopulatedResponse.setId(blog.getId());
			blogsPopulatedResponse.setTitle(blog.getTitle());
			blogsPopulatedResponse.setContent(blog.getContent());
			blogsPopulatedResponse.setAuthor(blogAuthor);
			blogResponse.setSuccess(true);
			blogResponse.setMessage("Blog with id:" + id + " fetched successfully.");
			blogResponse.setBlog(blogsPopulatedResponse);
			return new ResponseEntity<BlogResponseDTO>(blogResponse, HttpStatus.OK);

		} else {
			blogResponse.setSuccess(false);
			blogResponse.setMessage("Blog with id:" + id + " is not found.");
			blogResponse.setBlog(null);
			return new ResponseEntity<BlogResponseDTO>(blogResponse, HttpStatus.NOT_FOUND);
		}
	}

	@Operation(summary = "Update an existing blog", description = "Updates the details of a blog entry.")
	@ApiResponses(value = {
		@ApiResponse(responseCode = "200", description = "Blog updated successfully", 
			content = @Content(schema = @Schema(implementation = BlogResponseDTO.class))),
		@ApiResponse(responseCode = "403", description = "Forbidden: You do not have permission to update this blog", content = @Content),
		@ApiResponse(responseCode = "400", description = "Error while updating the blog", content = @Content)
	})
	@PutMapping("/updateExistingBlog/{id}")
	public ResponseEntity<BlogResponseDTO> updateBlog(
			@RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) @Parameter(hidden = true) String authorizationHeader,
			@PathVariable("id") String id, @RequestBody BlogRequestDTO blogRequest) {
		HttpEntity<String> entity = authorizeHeaders(authorizationHeader);
//		UserInfoResponse author = getLoggedInUserInfo(entity);
//		Blog blog = blogService.getBlogById(id);
		CompletableFuture<UserInfoResponse> user = getLoggedInUserInfo(entity);
		UserInfoResponse author = null;
		try {
			author = user.get();
		}
		catch(Exception e) {
			e.printStackTrace();
		}
		CompletableFuture<Blog> blog1 = blogService.getBlogById(id);
		Blog blog = null;
		try {
			blog = blog1.get();
		}
		catch(Exception e) {
			e.printStackTrace();
		}
		BlogPopulatedResponse blogsPopulatedResponse = new BlogPopulatedResponse();
		BlogResponseDTO blogResponse = new BlogResponseDTO();
		if (blog != null) {
			BlogAuthor blogAuthor = new BlogAuthor();
			if ((blog.getAuthorId().equals(author.getUserInfo().getId()))
					|| (author.getUserInfo().getRole().equals(Role.ADMIN))) {
				CompletableFuture<Blog> blog2 = blogService.updateBlog(blogRequest, id);
				Blog updatedBlog = null;
				try {
					updatedBlog = blog2.get();
				}
				catch(Exception e) {
					e.printStackTrace();
				}
//				Blog updatedBlog = blogService.updateBlog(blogRequest, id);
				if ((author.getUserInfo().getRole().equals(Role.ADMIN))
						&& (!blog.getAuthorId().equals(author.getUserInfo().getId()))) {
					CompletableFuture<UserInfoResponse> originalAuthor1 = getUserInfoByUserId(entity, blog.getAuthorId());
					UserInfoResponse originalAuthor = null;
					try {
						originalAuthor = originalAuthor1.get();
					}
					catch(Exception e) {
						e.printStackTrace();
					}
//					UserInfoResponse originalAuthor = getUserInfoByUserId(entity, blog.getAuthorId());
					blogAuthor.setId(originalAuthor.getUserInfo().getId());
					blogAuthor.setUsername(originalAuthor.getUserInfo().getUsername());
					blogAuthor.setRole(originalAuthor.getUserInfo().getRole());
				} else {
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

	@Operation(summary = "Delete a blog", description = "Deletes a blog entry by its ID.")
	@ApiResponses(value = {
		@ApiResponse(responseCode = "200", description = "Blog deleted successfully", 
			content = @Content(schema = @Schema(implementation = BlogResponseDTO.class))),
		@ApiResponse(responseCode = "403", description = "Forbidden: You do not have permission to delete this blog", content = @Content),
		@ApiResponse(responseCode = "404", description = "Blog not found to delete", content = @Content)
	})
	@DeleteMapping("/deleteExistingBlog/{id}")
	public ResponseEntity<BlogResponseDTO> deleteBlog(
			@RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) @Parameter(hidden = true) String authorizationHeader,
			@PathVariable("id") String id) {
		HttpEntity<String> entity = authorizeHeaders(authorizationHeader);
//		UserInfoResponse author = getLoggedInUserInfo(entity);
//		Blog blog = blogService.getBlogById(id);
		CompletableFuture<UserInfoResponse> user = getLoggedInUserInfo(entity);
		UserInfoResponse author = null;
		try {
			author = user.get();
		}
		catch(Exception e) {
			e.printStackTrace();
		}
		CompletableFuture<Blog> blog1 = blogService.getBlogById(id);
		Blog blog = null;
		try {
			blog = blog1.get();
		}
		catch(Exception e) {
			e.printStackTrace();
		}
		BlogResponseDTO blogResponse = new BlogResponseDTO();
		if (blog != null) {
			if ((blog.getAuthorId().equals(author.getUserInfo().getId()))
					|| (author.getUserInfo().getRole().equals(Role.ADMIN))) {
				CompletableFuture<String> deletedBlog1 = blogService.deleteBlog(id);
				String deletedBlog = null;
				try {
				  deletedBlog = deletedBlog1.get();	
				}
				catch(Exception e) {
				  e.printStackTrace();	
				}
//				String deletedBlog = blogService.deleteBlog(id);
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
				blogResponse.setMessage("Error while deleting this blog. You're trying to delete someone else's blog.");
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

	@Operation(summary = "Delete blogs of a deleted user", description = "Deletes all blogs associated with a deleted user.")
	@ApiResponses(value = {
		@ApiResponse(responseCode = "200", description = "Blogs of deleted user deleted successfully", 
			content = @Content(schema = @Schema(implementation = DefaultResponse.class))),
		@ApiResponse(responseCode = "403", description = "Forbidden: You do not have permission to delete blogs of deleted user", content = @Content)
	})
	@DeleteMapping("/deleteBlogsOfDeletedUser/{id}")
	public ResponseEntity<DefaultResponse> deleteBlogOfDeletedUser(
			@RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) @Parameter(hidden = true) String authorizationHeader,
			@PathVariable("id") Long id) {
		HttpEntity<String> entity = authorizeHeaders(authorizationHeader);
		CompletableFuture<UserInfoResponse> user = getLoggedInUserInfo(entity);
		UserInfoResponse userInfo = null;
		try {
			userInfo = user.get();
		}
		catch(Exception e) {
			e.printStackTrace();
		}
//		UserInfoResponse userInfo = getLoggedInUserInfo(entity);
		DefaultResponse defaultResponse = new DefaultResponse();
		if (userInfo.getUserInfo().getRole().equals(Role.ADMIN)) {
			CompletableFuture<String> deletedBlogsList = blogService.deleteAllBlogsOfDeletedUsers(id);
			String deletedBlogs = null;
			try {
			  deletedBlogs = deletedBlogsList.get();
			}
			catch(Exception e) {
			  e.printStackTrace();	
			}
			defaultResponse.setSuccess(true);
			defaultResponse.setMessage("Blogs of deleted user with user id:" + id + " are deleted successfully.");
			return new ResponseEntity<DefaultResponse>(defaultResponse, HttpStatus.OK);
		} else {
			defaultResponse.setSuccess(false);
			defaultResponse.setMessage("Trying to delete, deleted users blogs, but not an admin user to do so.");
			return new ResponseEntity<DefaultResponse>(defaultResponse, HttpStatus.FORBIDDEN);
		}
	}
}
