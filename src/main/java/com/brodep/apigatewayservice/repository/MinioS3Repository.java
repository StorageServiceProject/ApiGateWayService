package com.brodep.apigatewayservice.repository;

import com.brodep.apigatewayservice.configuration.beans.MinioProperties;
import com.brodep.apigatewayservice.dto.response.ResourceInfoResponse;
import com.brodep.apigatewayservice.exeption.AlreadyExistsException;
import com.brodep.apigatewayservice.exeption.ResourceNotFoundException;
import io.minio.*;
import io.minio.messages.Item;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Service
@Slf4j
@RequiredArgsConstructor
public class MinioS3Repository implements S3Repository {

    private MinioClient minioClient;

    private final MinioProperties minioProperties;
    private String bucketName;

    @PostConstruct
    public void init() {
        log.info("Initialisation connection to minio");
        try {
            minioClient = MinioClient.builder()
                    .endpoint(minioProperties.getHost())
                    .credentials(minioProperties.getUsername(), minioProperties.getPassword())
                    .build();
        } catch (Exception e) {
            log.error("Error connecting to minio");
        }
        log.info("Connection to minio is established");
        bucketName = minioProperties.getBucketName();
        log.info("Creating bucket with name {}", bucketName);
        try {
            var bucketExists = minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucketName).build());
            if (!bucketExists) {
                minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucketName).build());
            }
        } catch (Exception e) {
            log.error("Error creation Bucket {}", bucketName);
        }
        log.info("Bucket {} has been created", bucketName);
    }

    @Override
    public Set<ResourceInfoResponse> upload(String path, List<MultipartFile> files) {
        Set<ResourceInfoResponse> response = new CopyOnWriteArraySet<>();
        try (ExecutorService executorService = Executors.newVirtualThreadPerTaskExecutor()) {
            for (MultipartFile file : files) {
                executorService.submit(() -> {
                    log.info("Created new thread for uploading file {}", file.getOriginalFilename());
                    try {
                        minioClient.putObject(
                                PutObjectArgs.builder()
                                        .bucket(bucketName)
                                        .object(path + file.getOriginalFilename())
                                        .stream(file.getInputStream(), file.getSize(), -1)
                                        .contentType(file.getContentType())
                                        .build()
                        );
                        var stream = minioClient.getObject(
                                GetObjectArgs.builder()
                                        .bucket(bucketName)
                                        .object(path + file.getOriginalFilename())
                                        .build());
                        response.add(objectToResourceInfoResponse(stream));
                        log.info("File {} has been uploaded, thread is being closed", file.getOriginalFilename());
                    } catch (Exception e) {
                        log.error("Error uploading file {}", file.getOriginalFilename());
                        throw new AlreadyExistsException("Файл с именем %s уже существует".formatted(file.getName()));
                    }
                });
            }
        }
        return response;
    }

    @Override
    public void delete(String path) {
        try {
            minioClient.removeObject(
                    RemoveObjectArgs.builder()
                            .bucket(bucketName)
                            .object(path)
                            .build()
            );
        } catch (Exception e) {
            log.error("Error deleting file with path {}", path);
            throw new ResourceNotFoundException("Файл с путем %s не удалось удалить".formatted(path));
        }
    }

    @Override
    public byte[] download(String path) {
        try {
            var stream = minioClient.getObject(
                    GetObjectArgs.builder()
                            .bucket(bucketName)
                            .object(path)
                            .build());
            return stream.readAllBytes();
        } catch (Exception e) {
            log.error("Error downloading file with path {}", path);
            throw new ResourceNotFoundException("Файл с путем %s не удалось скачать".formatted(path));
        }
    }

    @Override
    public ResourceInfoResponse getInfo(String path) {
        if (path.endsWith("/")) {
            return new ResourceInfoResponse(
                    path,
                    path,
                    Optional.empty(),
                    ResourceInfoResponse.ResourceType.DIRECTORY
            );
        }
        try {
            var stream = minioClient.getObject(
                    GetObjectArgs.builder()
                            .bucket(bucketName)
                            .object(path)
                            .build());
            return objectToResourceInfoResponse(stream);
        } catch (Exception e) {
            log.error("Error getting info of file with path {}", path);
            throw new ResourceNotFoundException("Не удалось найти информацию о файле с путем %s".formatted(path));
        }
    }

    @Override
    public Set<ResourceInfoResponse> getDirectoryResources(String path) {
        var objects = minioClient.listObjects(
                ListObjectsArgs.builder()
                        .bucket(bucketName)
                        .recursive(true)
                        .build());
        Set<ResourceInfoResponse> response = new HashSet<>();
        try {
            for (Result<Item> itemResult : objects) {
                var curObject = itemResult.get();
                if (!curObject.isDir()) {
                    if (curObject.objectName().startsWith(path)) {
                        var stream = minioClient.getObject(
                                GetObjectArgs.builder()
                                        .bucket(bucketName)
                                        .object(curObject.objectName())
                                        .build());
                        response.add(objectToResourceInfoResponse(stream));
                    }
                }
            }
            return response;
        } catch (Exception e) {
            log.error("Error getting info of files in directory with path {}", path);
            throw new ResourceNotFoundException("Не удалось получить информацию о файлах с путем %s".formatted(path));
        }
    }

    /*
    @Override
    public void move(String from, String to) {
    }

    @Override
    public Set<ResourceInfoResponse> search(String query) {
        return Set.of();
    }

    @Override
    public void createDirectory() {

    }
     */

    private ResourceInfoResponse objectToResourceInfoResponse(GetObjectResponse stream) {
        var splitedPath = stream.object().split("/");
        var fileName = splitedPath[splitedPath.length - 1];
        var strBuilder = new StringBuilder(stream.object());
        var path = strBuilder.delete(strBuilder.lastIndexOf(fileName), stream.object().length()).toString();
        return new ResourceInfoResponse(
                path,
                fileName,
                Optional.of(stream.headers().size()),
                ResourceInfoResponse.ResourceType.FILE
        );
    }
}
