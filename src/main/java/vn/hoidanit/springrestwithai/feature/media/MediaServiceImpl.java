package vn.hoidanit.springrestwithai.feature.media;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import vn.hoidanit.springrestwithai.config.FileUploadProperties;
import vn.hoidanit.springrestwithai.dto.ResultPaginationDTO;
import vn.hoidanit.springrestwithai.exception.DuplicateResourceException;
import vn.hoidanit.springrestwithai.exception.ResourceNotFoundException;
import vn.hoidanit.springrestwithai.feature.file.FileService;
import vn.hoidanit.springrestwithai.feature.file.dto.FileUploadResponse;
import vn.hoidanit.springrestwithai.feature.media.dto.CreateMediaFolderRequest;
import vn.hoidanit.springrestwithai.feature.media.dto.MediaFilterRequest;
import vn.hoidanit.springrestwithai.feature.media.dto.MediaFolderResponse;
import vn.hoidanit.springrestwithai.feature.media.dto.MediaResponse;
import vn.hoidanit.springrestwithai.feature.media.dto.MoveMediaRequest;
import vn.hoidanit.springrestwithai.feature.media.dto.UpdateMediaFolderRequest;

@Service
@Transactional(readOnly = true)
public class MediaServiceImpl implements MediaService {

    private static final Logger log = LoggerFactory.getLogger(MediaServiceImpl.class);
    private static final String MEDIA_FOLDER = "media";

    private final MediaRepository mediaRepository;
    private final MediaFolderRepository mediaFolderRepository;
    private final FileService fileService;
    private final FileUploadProperties uploadProperties;

    public MediaServiceImpl(
            MediaRepository mediaRepository,
            MediaFolderRepository mediaFolderRepository,
            FileService fileService,
            FileUploadProperties uploadProperties) {
        this.mediaRepository = mediaRepository;
        this.mediaFolderRepository = mediaFolderRepository;
        this.fileService = fileService;
        this.uploadProperties = uploadProperties;
    }

    @Override
    @Transactional
    public MediaResponse upload(MultipartFile file, String title, Long userId) {
        return upload(file, title, null, userId);
    }

    @Override
    @Transactional
    public MediaResponse upload(MultipartFile file, String title, Long folderId, Long userId) {
        if (folderId != null && !mediaFolderRepository.existsById(folderId)) {
            throw new ResourceNotFoundException("MediaFolder", "id", folderId);
        }

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
        media.setFolderId(folderId);

        mediaRepository.save(media);
        log.info("Media uploaded: {} (folderId={}) by user {}", uploadResult.fileName(), folderId, userId);

        return MediaResponse.fromEntity(media);
    }

    @Override
    public ResultPaginationDTO findAll(MediaFilterRequest filter, Pageable pageable) {
        Page<Media> page = mediaRepository.findAll(MediaSpecification.build(filter), pageable);
        return ResultPaginationDTO.fromPage(page.map(MediaResponse::fromEntity));
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

    @Override
    @Transactional
    public void moveMedia(MoveMediaRequest request) {
        Long targetFolderId = request.folderId();
        if (targetFolderId != null && !mediaFolderRepository.existsById(targetFolderId)) {
            throw new ResourceNotFoundException("MediaFolder", "id", targetFolderId);
        }

        List<Media> mediaItems = mediaRepository.findAllById(request.mediaIds());
        for (Media item : mediaItems) {
            item.setFolderId(targetFolderId);
        }
        mediaRepository.saveAll(mediaItems);
        log.info("Moved {} media items to folderId={}", mediaItems.size(), targetFolderId);
    }

    @Override
    public List<MediaFolderResponse> getFolders(Long parentId, boolean all) {
        List<MediaFolder> folders;
        if (all) {
            folders = mediaFolderRepository.findAllByOrderByNameAsc();
        } else if (parentId != null) {
            folders = mediaFolderRepository.findByParentIdOrderByNameAsc(parentId);
        } else {
            folders = mediaFolderRepository.findByParentIdIsNullOrderByNameAsc();
        }

        List<MediaFolder> allFolders = all ? folders : mediaFolderRepository.findAll();
        Map<Long, Long> subfolderCounts = new HashMap<>();
        for (MediaFolder f : allFolders) {
            if (f.getParentId() != null) {
                subfolderCounts.merge(f.getParentId(), 1L, Long::sum);
            }
        }

        Map<Long, Long> itemCounts = new HashMap<>();
        for (Object[] row : mediaRepository.countGroupByFolderId()) {
            Long fId = (Long) row[0];
            Long count = (Long) row[1];
            if (fId != null) {
                itemCounts.put(fId, count);
            }
        }

        return folders.stream()
                .map(f -> MediaFolderResponse.fromEntity(
                        f,
                        itemCounts.getOrDefault(f.getId(), 0L),
                        subfolderCounts.getOrDefault(f.getId(), 0L)))
                .toList();
    }

    @Override
    @Transactional
    public MediaFolderResponse createFolder(CreateMediaFolderRequest request, Long userId) {
        String cleanName = request.name().trim();
        Long parentId = request.parentId();

        if (parentId != null && !mediaFolderRepository.existsById(parentId)) {
            throw new ResourceNotFoundException("MediaFolder", "id", parentId);
        }

        Optional<MediaFolder> existing = parentId != null
                ? mediaFolderRepository.findByNameIgnoreCaseAndParentId(cleanName, parentId)
                : mediaFolderRepository.findByNameIgnoreCaseAndParentIdIsNull(cleanName);

        if (existing.isPresent()) {
            MediaFolder folder = existing.get();
            long itemCount = mediaRepository.countByFolderId(folder.getId());
            long subCount = mediaFolderRepository.findByParentId(folder.getId()).size();
            return MediaFolderResponse.fromEntity(folder, itemCount, subCount);
        }

        MediaFolder folder = new MediaFolder();
        folder.setName(cleanName);
        folder.setParentId(parentId);
        folder.setCreatedBy(userId);
        mediaFolderRepository.save(folder);

        log.info("MediaFolder created: id={}, name={}, parentId={}", folder.getId(), cleanName, parentId);
        return MediaFolderResponse.fromEntity(folder, 0L, 0L);
    }

    @Override
    @Transactional
    public MediaFolderResponse updateFolder(Long id, UpdateMediaFolderRequest request) {
        MediaFolder folder = mediaFolderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("MediaFolder", "id", id));

        String cleanName = request.name().trim();
        Long parentId = request.parentId() != null ? request.parentId() : folder.getParentId();

        if (parentId != null && parentId.equals(id)) {
            throw new IllegalArgumentException("Thư mục không thể là thư mục cha của chính nó");
        }

        Optional<MediaFolder> duplicate = parentId != null
                ? mediaFolderRepository.findByNameIgnoreCaseAndParentId(cleanName, parentId)
                : mediaFolderRepository.findByNameIgnoreCaseAndParentIdIsNull(cleanName);

        if (duplicate.isPresent() && !duplicate.get().getId().equals(id)) {
            throw new DuplicateResourceException("Thư mục", "name", cleanName);
        }

        folder.setName(cleanName);
        if (request.parentId() != null || folder.getParentId() != null) {
            folder.setParentId(request.parentId());
        }
        mediaFolderRepository.save(folder);

        long itemCount = mediaRepository.countByFolderId(folder.getId());
        long subCount = mediaFolderRepository.findByParentId(folder.getId()).size();
        return MediaFolderResponse.fromEntity(folder, itemCount, subCount);
    }

    @Override
    @Transactional
    public void deleteFolder(Long id) {
        MediaFolder folder = mediaFolderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("MediaFolder", "id", id));

        Long fallbackParentId = folder.getParentId();

        // Di chuyển các thư mục con lên cấp cha
        List<MediaFolder> childFolders = mediaFolderRepository.findByParentId(id);
        for (MediaFolder child : childFolders) {
            child.setParentId(fallbackParentId);
        }
        mediaFolderRepository.saveAll(childFolders);

        // Di chuyển các media trong thư mục này ra thư mục cha / gốc (không làm mất file)
        List<Media> mediaInFolder = mediaRepository.findByFolderId(id);
        for (Media m : mediaInFolder) {
            m.setFolderId(fallbackParentId);
        }
        mediaRepository.saveAll(mediaInFolder);

        mediaFolderRepository.delete(folder);
        log.info("MediaFolder deleted: id={}, moved {} files to folderId={}", id, mediaInFolder.size(), fallbackParentId);
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
