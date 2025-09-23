package com.example.chat.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.example.chat.dto.res.UploadImageResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class CloudinaryService {
    private final Cloudinary cloudinary;

    public UploadImageResponse uploadFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("File không được rỗng");
        }

        try {
            Map<?, ?> uploadResult = cloudinary.uploader().upload(file.getBytes(), ObjectUtils.emptyMap());

            String fileUrl = (String) uploadResult.get("secure_url");
            String originalFilename = file.getOriginalFilename();
            Long fileSize = file.getSize();
            String contentType = file.getContentType();

            return new UploadImageResponse(
                    originalFilename,
                    fileUrl,
                    fileSize,
                    contentType
            );
        } catch (IOException e) {
            log.error("Lỗi khi upload file lên Cloudinary", e);
            throw new RuntimeException("Upload ảnh thất bại", e);
        }
    }
}
