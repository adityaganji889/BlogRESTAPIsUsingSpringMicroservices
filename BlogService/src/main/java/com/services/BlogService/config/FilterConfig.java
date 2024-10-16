package com.services.BlogService.config;

import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FilterConfig {

	    @Bean
	    public FilterRegistrationBean<JwtAuthenticationFilter> jwtFilter() {
	        FilterRegistrationBean<JwtAuthenticationFilter> registrationBean = new FilterRegistrationBean<>();
	        registrationBean.setFilter(new JwtAuthenticationFilter());
	        registrationBean.addUrlPatterns("/api/blogs/*"); // Specify your blog endpoints here
	        registrationBean.setOrder(1); // Set the order if needed (lower values have higher priority)
	        return registrationBean;
	    }
}
