package com.services.UserService.config;

import java.util.Collections;

import org.springdoc.core.customizers.OpenApiCustomiser;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeIn;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.info.License;
//import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.servers.Server;

@OpenAPIDefinition(
  info = @Info(
	  contact = @Contact (
		  name = "Aditya Ganji",
		  email = "adityaganji889@gmail.com"
		),
	  description = "OpenApi documentation for User Microservice",
	  title="User Microservice REST API Documentation",
      license = @License (
    	   name = "Licensed By Aditya Ganji"
      ),
	  termsOfService = "Terms of service"
	),
    servers = {
    	@Server(
    		description = "Local Env",
    		url = "http://localhost:8081"
    	)// similar add another entry of Server for Prod Env.	
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
@Configuration
public class OpenApiConfig {
	
	@Bean
    public OpenApiCustomiser customizeOpenApi() {
        return openApi -> {
            // Remove security requirement for specific paths
            openApi.getPaths().forEach((path, pathItem) -> {
                if (path.startsWith("/api/users")) {
                    pathItem.readOperationsMap().forEach((httpMethod, operation) -> {
                    	// Log to confirm the operation is being modified
                        System.out.println("Removing security for path: " + path + " Method: " + httpMethod);

                        // Clear security requirements for this operation
                        operation.setSecurity(Collections.emptyList());
                    });
                }
            });
        };
    }
}
