package com.example.chat.integration.minio;

import io.minio.*;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class MinioChannel {

    private final MinioProps props;
    private final MinioClient minioClient;

    @PostConstruct
    private void init() {
        try {
            createBucketIfNeeded(props.getBucket(), props.isMakeBucketPublic());
        } catch (Exception e) {
            throw new RuntimeException("Không thể kiểm tra/tạo bucket " + props.getBucket(), e);
        }
    }

    private void createBucketIfNeeded(final String name, boolean makePublic) throws Exception {
        boolean exists = minioClient.bucketExists(
                BucketExistsArgs.builder().bucket(name).build()
        );
        if (!exists) {
            minioClient.makeBucket(MakeBucketArgs.builder().bucket(name).build());
            if (makePublic) {
                final var policy = """
                        {
                          "Version": "2012-10-17",
                          "Statement": [{
                            "Effect": "Allow",
                            "Principal": "*",
                            "Action": "s3:GetObject",
                            "Resource": "arn:aws:s3:::%s/*"
                          }]
                        }
                        """.formatted(name);
                minioClient.setBucketPolicy(
                        SetBucketPolicyArgs.builder().bucket(name).config(policy).build()
                );
            }
        }
    }

    /** Sinh objectKey duy nhất: {prefix/}{uuid}-{filename} */
    public String buildObjectKey(String originalName) {
        String safeName = (originalName == null || originalName.isBlank())
                ? "file.bin"
                : originalName.strip().replace("\\", "/");
        String nameOnly = safeName.substring(safeName.lastIndexOf('/') + 1);

        String prefix = props.getKeyPrefix() == null ? "" : props.getKeyPrefix().trim();
        if (!prefix.isEmpty() && !prefix.endsWith("/")) {
            prefix += "/";
        }
        return prefix + UUID.randomUUID() + "-" + nameOnly;
    }

    /** Presigned URL PUT (FE upload trực tiếp file lên MinIO) */
    public String presignedPutUrl(String objectKey, int ttlSeconds) throws Exception {
        return minioClient.getPresignedObjectUrl(
                GetPresignedObjectUrlArgs.builder()
                        .method(io.minio.http.Method.PUT)
                        .bucket(props.getBucket())
                        .object(objectKey)
                        .expiry(ttlSeconds > 0 ? ttlSeconds : props.getPresignExpirySeconds())
                        .build()
        );
    }

    /** Presigned URL GET (FE tải file từ MinIO) */
    public String presignedGetUrl(String objectKey, int ttlSeconds) throws Exception {
        return minioClient.getPresignedObjectUrl(
                GetPresignedObjectUrlArgs.builder()
                        .method(io.minio.http.Method.GET)
                        .bucket(props.getBucket())
                        .object(objectKey)
                        .expiry(ttlSeconds > 0 ? ttlSeconds : props.getPresignExpirySeconds())
                        .build()
        );
    }
}
