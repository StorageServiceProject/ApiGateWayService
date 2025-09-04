package com.brodep.apigatewayservice.service;

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

    private String getUserPath(String path) {
        log.info("");
        var userId = userService.getCurrentUser().getId().toString();
        if (path == null) {
            return "user-%s-files/".formatted(userId);
        }
        return "user-%s-files/%s".formatted(userId, path);
    }

    public ResourceInfoResponse getResourceInfo(String path) {
        log.info("");
        return s3Repository.getInfo(getUserPath(path));
    }

    public void deleteResource(String path) {
        log.info("");
        s3Repository.delete(path);
    }

    public byte[] downloadResource(String path) {
        log.info("");
        return s3Repository.download(path);
    }

    public Set<ResourceInfoResponse> uploadResources(String path, List<MultipartFile> files) {
        log.info("");
        return s3Repository.upload(getUserPath(path), files);
    }

    public Set<ResourceInfoResponse> getDirectoryResources(String path) {
        log.info("");
        return s3Repository.getDirectoryResources(path);
    }
}
