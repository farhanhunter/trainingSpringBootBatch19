package com.example.demo.util;

import com.example.demo.exception.FileStorageException;
import io.minio.*;
import io.minio.http.Method;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.ByteArrayInputStream;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
@RequiredArgsConstructor(onConstructor = @__( @Autowired))
public class MinioUtil {

    private final MinioClient minioClient;

    @Value("${minio.bucket.name}")
    private String bucketName;

    @Value("${minio.presigned-expiry-minutes}")
    private int presignedExpiryMinutes;

    private void pastikanBucketAda() throws Exception {
        boolean ada = minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucketName).build());
        if (!ada) {
            minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucketName).build());
            log.info("Bucket {} dibuat", bucketName);
        }
    }

    public void upload(String namaFile, byte[] isi, String contentType) {
        try {
            pastikanBucketAda();
            minioClient.putObject(PutObjectArgs.builder()
                    .bucket(bucketName)
                    .object(namaFile)
                    .stream(new ByteArrayInputStream(isi), isi.length, -1)
                    .contentType(contentType)
                    .build());
            log.info("Upload {} ke bucket {} berhasil", namaFile, bucketName);
        } catch (Exception e) {
            throw new FileStorageException("Gagal upload ke MinIO: " + e.getMessage());
        }
    }

    public String linkDownload(String namaFile) {
        try {
            return minioClient.getPresignedObjectUrl(GetPresignedObjectUrlArgs.builder()
                    .method(Method.GET)
                    .bucket(bucketName)
                    .object(namaFile)
                    .expiry(presignedExpiryMinutes, TimeUnit.MINUTES)
                    .build());
        } catch (Exception e) {
            throw new FileStorageException("Gagal membuat link download: " + e.getMessage());
        }
    }
}
