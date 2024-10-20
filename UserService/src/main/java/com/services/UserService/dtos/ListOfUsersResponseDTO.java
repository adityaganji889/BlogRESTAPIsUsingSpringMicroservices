package com.services.UserService.dtos;

import java.util.List;
import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.Data;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL) // This will ignore null fields
public class ListOfUsersResponseDTO {

	private boolean success;
	private String message;
	private List<UserInfo> users;
}
