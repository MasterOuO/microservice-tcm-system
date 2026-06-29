package com.tcmdiff.SpringTcmDiff.configuration;


import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer{
	@Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
		
		// 從專案root開始找
		registry.addResourceHandler("/diffgraph/**")
        		.addResourceLocations("file:./diffgraph/");
    }
}
