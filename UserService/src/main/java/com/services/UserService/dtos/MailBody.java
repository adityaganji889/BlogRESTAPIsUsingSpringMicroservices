package com.services.UserService.dtos;

import lombok.Builder;

@Builder
public record MailBody(String to, String subject, String text) {
	
}
