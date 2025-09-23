package com.example.chat.integration.minio;

import com.example.chat.enums.FileType;
import com.example.chat.integration.minio.dto.PresignPutRequest;
import com.example.chat.integration.minio.dto.UploadResult;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/storage")
@RequiredArgsConstructor
public class StorageController {
    private final MinioChannel minioChannel;
    private final MinioProps props;

    /**
     * FE gửi danh sách file (filename + contentType)
     * BE trả về danh sách presigned PUT URLs để FE upload trực tiếp lên MinIO
     */
    @PostMapping("/upload-urls")
    public List<UploadResult> getUploadUrls(@RequestBody List<PresignPutRequest> files,
                                            @RequestParam(defaultValue = "0") int ttlSeconds) throws Exception {
        int ttl = ttlSeconds > 0 ? ttlSeconds : props.getPresignExpirySeconds();
        List<UploadResult> result = new ArrayList<>();

        for (PresignPutRequest file : files) {
            String objectKey = minioChannel.buildObjectKey(file.getFileName());
            String url = minioChannel.presignedPutUrl(objectKey, ttl);

            FileType fileType = mapContentTypeToFileType(file.getContentType());

            result.add(UploadResult.builder()
                    .objectKey(objectKey)
                    .uploadUrl(url)
                    .fileType(fileType)
                    .build()
            );
        }
        return result;
    }

    /**
     * Hàm map contentType → FileType (IMAGE, VIDEO, FILE, NONE)
     */
    private FileType mapContentTypeToFileType(String contentType) {
        if (contentType == null || contentType.isBlank()) {
            return FileType.NONE;
        }

        if (contentType.startsWith("image/")) {
            return FileType.IMAGE;
        }

        if (contentType.startsWith("video/")) {
            return FileType.VIDEO;
        }

        // Các loại file tài liệu phổ biến
        if (contentType.equals("application/pdf") ||
                contentType.equals("application/msword") ||
                contentType.equals("application/vnd.openxmlformats-officedocument.wordprocessingml.document") ||
                contentType.equals("application/vnd.ms-excel") ||
                contentType.equals("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet") ||
                contentType.equals("application/vnd.ms-powerpoint") ||
                contentType.equals("application/vnd.openxmlformats-officedocument.presentationml.presentation")) {
            return FileType.FILE;
        }

        return FileType.FILE; // fallback
    }
}
