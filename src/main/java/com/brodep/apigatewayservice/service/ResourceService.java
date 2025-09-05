package com.brodep.apigatewayservice.service;

import com.brodep.apigatewayservice.dto.event.MinioEvent;
import com.brodep.apigatewayservice.dto.response.ResourceInfoResponse;
import com.brodep.apigatewayservice.repository.S3Repository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
public class ResourceService {

    private final UserService userService;
    private final S3Repository s3Repository;
    private final KafkaProducerService kafkaProducerService;

    private String getUserPath(String path) {
        log.info("");
        var userId = userService.getCurrentUser().getId().toString();
        if (path == null) {
            return "user-%s-files/".formatted(userId);
        }
        return "user-%s-files/%s".formatted(userId, path);
    }

    public ResourceInfoResponse getResourceInfo(String path) {
        var res = s3Repository.getInfo(getUserPath(path));
        kafkaProducerService.sendResourceInfoRequestedEvent(getUserPath(path), new MinioEvent(
                1
        ));
        return res;
    }

    public void deleteResource(String path) {
        s3Repository.delete(path);
        kafkaProducerService.sendResourceDeletedEvent(path, new MinioEvent(
                1
        ));
    }

    public byte[] downloadResource(String path) {
        var res = s3Repository.download(path);
        kafkaProducerService.sendResourceDownloadedEvent(path, new MinioEvent(
                1
        ));
        return res;
    }

    public Set<ResourceInfoResponse> uploadResources(String path, List<MultipartFile> files) {
        var res = s3Repository.upload(getUserPath(path), files);
        kafkaProducerService.sendResourceUploadedEvent(getUserPath(path), new MinioEvent(
                res.size()
        ));
        return res;
    }

    public Set<ResourceInfoResponse> getDirectoryResources(String path) {
        var res = s3Repository.getDirectoryResources(path);
        kafkaProducerService.sendDirectoryResourcesInfoRequestedEvent(path, new MinioEvent(
                res.size()
        ));
        return res;
    }
}
