package com.example.backend;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@SpringBootApplication
public class BackendApplication {

	public static void main(String[] args) {
		SpringApplication.run(BackendApplication.class, args);
	}

	// Movie/news poster uploads are written to file.upload-dir (see MovieImpl/
	// NewsImpl) and served back under /img/**, but nothing previously registered
	// that directory as a static resource location: Spring Boot only serves
	// classpath:/static/** by default, so any uploaded image 404'd (which
	// GlobalExceptionHandler's catch-all then turned into a misleading 500).
	@Bean
	public WebMvcConfigurer uploadedImageResourceConfigurer(@Value("${file.upload-dir}") String uploadDir) {
		return new WebMvcConfigurer() {
			@Override
			public void addResourceHandlers(ResourceHandlerRegistry registry) {
				// Check the runtime upload directory first, then fall back to the
				// bundled classpath images (the seed-data posters), so registering
				// this more specific /img/** handler doesn't shadow those.
				registry.addResourceHandler("/img/**")
						.addResourceLocations("file:" + uploadDir + "/", "classpath:/static/img/");
			}
		};
	}
}
