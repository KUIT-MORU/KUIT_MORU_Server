package com.moru.backend.global.util;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;

import java.io.IOException;
import java.net.URI;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class S3Service {
    private final S3Client s3Client;

    @Value("${spring.cloud.aws.s3.bucket}")
    private String bucket;

    private final boolean usePresigned = false;

    public String uploadFile(MultipartFile file) throws IOException {
        String key =  S3Directory.TEMP.getDirName() + UUID.randomUUID() + "-" + file.getOriginalFilename();

        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket(bucket)
                .key(key)
                .contentType(file.getContentType())
                .build();

        s3Client.putObject(putObjectRequest, RequestBody.fromInputStream(file.getInputStream(), file.getSize()));

        return key;
    }

    public String moveToRealLocation(String key, S3Directory directory) {
        // key에 해당하는 이미지가 존재하는지 확인
        try {
            s3Client.headObject(HeadObjectRequest.builder()
                    .bucket(bucket)
                    .key(key)
                    .build());
        } catch (S3Exception e) {
            if(e.statusCode() == 404) {
                // 존재하지 않는 객체인 경우 null 반환 또는 예외
                return null;
            }
            throw e;
        }

        // 복사하기
        String newKey = copyObject(key, directory);

        deleteObject(key);
        return newKey;

    }

    public String copyObject(String key, S3Directory directory) {
        String newKey = directory.getDirName() + key.substring(key.lastIndexOf("/") + 1);

        CopyObjectRequest copyObjectRequest = CopyObjectRequest.builder()
                .sourceBucket(bucket)
                .sourceKey(key)
                .destinationBucket(bucket)
                .destinationKey(newKey)
                .build();
        s3Client.copyObject(copyObjectRequest);
        return newKey;
    }

    public void deleteObject(String key) {
        s3Client.deleteObject(DeleteObjectRequest.builder()
                .bucket(bucket)
                .key(key)
                .build());
    }

    public String getImageUrl(String key) {
        if(usePresigned) {
            return generatePresignedUrl(key);
        } else {
            return getFullUrl(key);
        }
    }

    private String getFullUrl(String key) {
        if (key == null || key.isBlank()) {
            return null; // null 또는 빈 키는 그대로 반환
        }


        if (key == null || key.isBlank()) {
            return key;
        }

        // [개선] S3Client에 설정된 엔드포인트를 사용하여 URL을 생성하여 일관성 유지
        String endpoint = s3Client.serviceClientConfiguration().endpointOverride()
                .map(URI::toString)
                .orElse("https://s3.amazonaws.com"); // 기본값

        // endpoint가 /로 끝나면 중복 슬래시 방지
        if (endpoint.endsWith("/")) {
            endpoint = endpoint.substring(0, endpoint.length() - 1);
        }

        // path-style 접근을 사용하므로 URL 형식은 endpoint/bucket/key
        return String.format("%s/%s/%s", endpoint, bucket, key);
    }

    private String generatePresignedUrl(String key) {
        // presigned 로직은 나중에 필요 시 구현
        throw new UnsupportedOperationException("Presigned URL 기능은 아직 구현되지 않았습니다.");
    }
}
