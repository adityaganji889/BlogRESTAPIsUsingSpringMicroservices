package com.services.BlogService.dtos;

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
public class BlogPopulatedResponse {

	private String id;
	private String title;
	private String content;
	private BlogAuthor author;
	
}
