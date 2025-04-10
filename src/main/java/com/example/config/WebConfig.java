package com.example.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.*;

@Configuration
public class WebConfig implements WebMvcConfigurer {
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**") // Permite todos los endpoints
            .allowedOrigins("http://localhost:3000") // Aquí va el frontend
            .allowedMethods("*") // GET, POST, PUT, DELETE, etc.
            .allowedHeaders("*");
    }
}
