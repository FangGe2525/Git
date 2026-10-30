package com.community.repair.controller;

import com.community.repair.common.ApiResponse;
import com.community.repair.service.UploadService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
@RequestMapping("/api/upload")
@RequiredArgsConstructor
public class UploadController {

    private final UploadService uploadService;

    /** 直传单个文件，返回可直接访问的 URL */
    @PostMapping
    public ApiResponse<Map<String, Object>> upload(@RequestParam("file") MultipartFile file) {
        String url = uploadService.store(file);
        return ApiResponse.ok(Map.of(
                "url", url,
                "size", file.getSize(),
                "name", file.getOriginalFilename() == null ? "" : file.getOriginalFilename()));
    }
}
