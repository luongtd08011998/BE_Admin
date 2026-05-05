package vn.hoidanit.springrestwithai.feature.media;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import vn.hoidanit.springrestwithai.config.FileUploadProperties;
import vn.hoidanit.springrestwithai.dto.ResultPaginationDTO;
import vn.hoidanit.springrestwithai.exception.ResourceNotFoundException;
import vn.hoidanit.springrestwithai.feature.file.FileService;
import vn.hoidanit.springrestwithai.feature.file.dto.FileUploadResponse;
import vn.hoidanit.springrestwithai.feature.media.dto.MediaFilterRequest;
import vn.hoidanit.springrestwithai.feature.media.dto.MediaResponse;

@Service
@Transactional(readOnly = true)
public class MediaServiceImpl implements MediaService {

    private static final Logger log = LoggerFactory.getLogger(MediaServiceImpl.class);
    private static final String MEDIA_FOLDER = "media";

    private final MediaRepository mediaRepository;
    private final FileService fileService;
    private final FileUploadProperties uploadProperties;

    public MediaServiceImpl(MediaRepository mediaRepository, FileService fileService,
            FileUploadProperties uploadProperties) {
        this.mediaRepository = mediaRepository;
        this.fileService = fileService;
        this.uploadProperties = uploadProperties;
    }

    @Override
    @Transactional
    public MediaResponse upload(MultipartFile file, String title, Long userId) {
        FileUploadResponse uploadResult = fileService.upload(file, MEDIA_FOLDER);

        String originalName = file.getOriginalFilename();
        String extension = getExtension(originalName);

        Media media = new Media();
        media.setTitle(title != null && !title.isBlank() ? title : originalName);
        media.setFileName(originalName);
        media.setFileUrl(uploadResult.fileUrl());
        media.setFileType(extension);
        media.setFileSize(file.getSize());
        media.setUploadedBy(userId);

        mediaRepository.save(media);
        log.info("Media uploaded: {} by user {}", uploadResult.fileName(), userId);

        return MediaResponse.fromEntity(media);
    }

    @Override
    public ResultPaginationDTO findAll(MediaFilterRequest filter, Pageable pageable) {
        Page<Media> page = mediaRepository.findAll(MediaSpecification.build(filter), pageable);
        return ResultPaginationDTO.fromPage(page);
    }

    @Override
    public MediaResponse findById(Long id) {
        Media media = mediaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Media", "id", id));
        return MediaResponse.fromEntity(media);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        Media media = mediaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Media", "id", id));

        deletePhysicalFile(media.getFileUrl());
        mediaRepository.delete(media);
        log.info("Media deleted: id={}, file={}", id, media.getFileUrl());
    }

    private void deletePhysicalFile(String fileUrl) {
        String relativePath = fileUrl.replace("/uploads/", "");
        Path filePath = uploadProperties.getUploadRoot().resolve(relativePath);
        try {
            Files.deleteIfExists(filePath);
        } catch (IOException e) {
            log.warn("Could not delete physical file: {} — {}", filePath, e.getMessage());
        }
    }

    private String getExtension(String fileName) {
        if (fileName == null) {
            return "";
        }
        int dotIndex = fileName.lastIndexOf('.');
        if (dotIndex < 0 || dotIndex == fileName.length() - 1) {
            return "";
        }
        return fileName.substring(dotIndex + 1).toLowerCase();
    }
}
