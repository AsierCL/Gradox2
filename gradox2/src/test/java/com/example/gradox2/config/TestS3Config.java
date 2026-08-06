package com.example.gradox2.config;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;

import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.CreateBucketRequest;
import software.amazon.awssdk.services.s3.model.CreateBucketResponse;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.DeleteObjectResponse;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.HeadBucketRequest;
import software.amazon.awssdk.services.s3.model.HeadBucketResponse;
import software.amazon.awssdk.services.s3.model.NoSuchBucketException;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectResponse;

@Configuration
@Profile("test")
public class TestS3Config {

    @Bean
    @Primary
    public S3Client s3Client(@Value("${s3.bucket-name}") String bucketName) {
        return new InMemoryS3Client(bucketName);
    }

    static class InMemoryS3Client implements S3Client {
        private final Map<String, byte[]> objects = new ConcurrentHashMap<>();
        private final Set<String> buckets = ConcurrentHashMap.newKeySet();
        private final String configuredBucket;

        InMemoryS3Client(String configuredBucket) {
            this.configuredBucket = configuredBucket;
        }

        @Override
        public CreateBucketResponse createBucket(CreateBucketRequest createBucketRequest) {
            buckets.add(createBucketRequest.bucket());
            return CreateBucketResponse.builder().build();
        }

        @Override
        public HeadBucketResponse headBucket(HeadBucketRequest headBucketRequest) {
            if (buckets.contains(headBucketRequest.bucket()) || configuredBucket.equals(headBucketRequest.bucket())) {
                return HeadBucketResponse.builder().build();
            }
            throw NoSuchBucketException.builder().message("The specified bucket does not exist").statusCode(404).build();
        }

        @Override
        public PutObjectResponse putObject(PutObjectRequest putObjectRequest, RequestBody requestBody) {
            try (InputStream in = requestBody.contentStreamProvider().newStream()) {
                objects.put(putObjectRequest.key(), in.readAllBytes());
            } catch (IOException e) {
                throw new IllegalStateException("Error reading request body", e);
            }
            return PutObjectResponse.builder().build();
        }

        @Override
        public ResponseInputStream<GetObjectResponse> getObject(GetObjectRequest getObjectRequest) {
            byte[] data = objects.get(getObjectRequest.key());
            if (data == null) {
                throw NoSuchKeyException.builder().message("The specified key does not exist").statusCode(404).build();
            }
            return new ResponseInputStream<>(GetObjectResponse.builder().build(), new ByteArrayInputStream(data));
        }

        @Override
        public DeleteObjectResponse deleteObject(DeleteObjectRequest deleteObjectRequest) {
            objects.remove(deleteObjectRequest.key());
            return DeleteObjectResponse.builder().build();
        }

        @Override
        public String serviceName() {
            return S3Client.SERVICE_NAME;
        }

        @Override
        public void close() {
        }
    }
}
