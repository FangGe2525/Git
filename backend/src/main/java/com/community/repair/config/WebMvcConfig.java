package com.community.repair.config;

import com.community.repair.service.UploadService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * 把上传目录映射为 /uploads/** 静态资源，便于前端直接展示图片。
 */
@Configuration
@RequiredArgsConstructor
public class WebMvcConfig implements WebMvcConfigurer {

    private final UploadService uploadService;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        String location = uploadService.getRoot().toUri().toString();
        registry.addResourceHandler("/uploads/**").addResourceLocations(location);
    }
}
