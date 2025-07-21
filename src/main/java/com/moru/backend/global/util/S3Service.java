package com.moru.backend.global.util;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;

import java.io.IOException;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class S3Service {
    private final S3Client s3Client;

    @Value("${spring.cloud.aws.s3.bucket}")
    private String bucket;

    @Value("${spring.cloud.aws.region.static}")
    private String region;

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

        // 삭제
        DeleteObjectRequest deleteObjectRequest = DeleteObjectRequest.builder()
                .bucket(bucket)
                .key(key)
                .build();
        s3Client.deleteObject(deleteObjectRequest);
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

    public String getImageUrl(String key) {
        if(usePresigned) {
            return generatePresignedUrl(key);
        } else {
            return getFullUrl(key);
        }
    }

    private String getFullUrl(String key) {
        StringBuilder url = new StringBuilder();
        url.append("https://")
                .append(bucket)
                .append(".s3.")
                .append(region)
                .append(".amazonaws.com/")
                .append(key);
        return url.toString();
    }

    private String generatePresignedUrl(String key) {
        // presigned 로직은 나중에 필요 시 구현
        throw new UnsupportedOperationException("Presigned URL 기능은 아직 구현되지 않았습니다.");
    }
}
