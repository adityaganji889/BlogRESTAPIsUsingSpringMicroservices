package com.services.BlogService.dtos;

import java.util.List;
import com.services.BlogService.entities.Blog;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Data
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class BlogsListResponseDTO {

	private boolean success;
	private String message;
	private List<BlogPopulatedResponse> blogs;
	
}
