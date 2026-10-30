package com.community.repair.service;

import com.community.repair.common.BusinessException;
import com.community.repair.common.ErrorCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

/**
 * 本地文件存储。第一版为直传，分片上传后续再加。
 */
@Slf4j
@Service
public class UploadService {

    private static final Set<String> IMAGE_EXT = Set.of("jpg", "jpeg", "png", "webp", "gif");
    private static final Set<String> VIDEO_EXT = Set.of("mp4", "mov", "webm");
    private static final long IMAGE_MAX = 5L * 1024 * 1024;
    private static final long VIDEO_MAX = 20L * 1024 * 1024;
    private static final DateTimeFormatter MONTH = DateTimeFormatter.ofPattern("yyyyMM");

    private final Path root;

    public UploadService(@Value("${app.upload.dir:./data/uploads}") String uploadDir) throws IOException {
        this.root = Paths.get(uploadDir).toAbsolutePath().normalize();
        Files.createDirectories(root);
        log.info("上传目录: {}", root);
    }

    public String store(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "文件为空");
        }
        String ext = extensionOf(file.getOriginalFilename());
        boolean isImage = IMAGE_EXT.contains(ext);
        boolean isVideo = VIDEO_EXT.contains(ext);
        if (!isImage && !isVideo) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "只支持 jpg/png/webp/gif 图片与 mp4/mov/webm 视频");
        }
        long limit = isImage ? IMAGE_MAX : VIDEO_MAX;
        if (file.getSize() > limit) {
            throw new BusinessException(ErrorCode.BAD_REQUEST,
                    "文件超过大小限制（" + (isImage ? "图片 5MB" : "视频 20MB") + "）");
        }

        String month = LocalDate.now().format(MONTH);
        Path dir = root.resolve(month);
        String name = UUID.randomUUID().toString().replace("-", "") + "." + ext;
        Path target = dir.resolve(name).normalize();
        if (!target.startsWith(root)) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "非法路径");
        }
        try {
            Files.createDirectories(dir);
            try (InputStream in = file.getInputStream()) {
                Files.copy(in, target, StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (IOException ex) {
            log.error("保存文件失败", ex);
            throw new BusinessException("文件保存失败");
        }
        return "/uploads/" + month + "/" + name;
    }

    public Path getRoot() {
        return root;
    }

    private String extensionOf(String filename) {
        if (!StringUtils.hasText(filename)) {
            return "";
        }
        int dot = filename.lastIndexOf('.');
        if (dot < 0 || dot == filename.length() - 1) {
            return "";
        }
        return filename.substring(dot + 1).toLowerCase(Locale.ROOT);
    }
}
