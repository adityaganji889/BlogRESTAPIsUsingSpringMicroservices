package com.services.BlogService.dtos;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;

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
@JsonInclude(JsonInclude.Include.NON_NULL) // This will ignore null fields
public class BlogsOfAuthorListResponseDTO {

	private boolean success;
	private String message;
	private BlogAuthor author;
	private List<BlogResponse> blogs;
}
