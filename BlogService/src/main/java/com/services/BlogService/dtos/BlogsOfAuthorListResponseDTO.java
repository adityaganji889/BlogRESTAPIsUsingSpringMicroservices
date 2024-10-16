package com.services.BlogService.dtos;

import java.util.List;

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
public class BlogsOfAuthorListResponseDTO {

	private boolean success;
	private String message;
	private BlogAuthor author;
	private List<BlogResponse> blogs;
}
