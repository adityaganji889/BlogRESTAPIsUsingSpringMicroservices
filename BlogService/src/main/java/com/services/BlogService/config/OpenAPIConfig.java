package com.services.BlogService.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeIn;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.info.License;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.servers.Server;

@OpenAPIDefinition(
		  info = @Info(
			  contact = @Contact (
				  name = "Aditya Ganji",
				  email = "adityaganji889@gmail.com"
				),
			  description = "OpenApi documentation for Blog Microservice",
			  title="Blog Microservice REST API Documentation",
		      license = @License (
		    	   name = "Licensed By Aditya Ganji"
		      ),
			  termsOfService = "Terms of service"
			),
		    servers = {
		    	@Server(
		    		description = "Local Env",
		    		url = "http://localhost:8082"
		    	)// similar add another entry of Server for Prod Env.	
		    },
		  security = {
			    	@SecurityRequirement(
			    		name="BearerAuth"	//Enabling Security Requirement Globally.
			    	)
			    }
)
@SecurityScheme(
		name="BearerAuth",
		description="JWT Auth Description",
		scheme="bearer",
		type = SecuritySchemeType.HTTP,
		bearerFormat="JWT",
		in = SecuritySchemeIn.HEADER // injects JWT Token in the header
	)
public class OpenAPIConfig {

}
