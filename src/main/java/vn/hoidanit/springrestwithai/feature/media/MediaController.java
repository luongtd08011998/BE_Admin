package vn.hoidanit.springrestwithai.feature.media;

import java.util.List;

import jakarta.validation.Valid;

import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import vn.hoidanit.springrestwithai.dto.ApiResponse;
import vn.hoidanit.springrestwithai.dto.ResultPaginationDTO;
import vn.hoidanit.springrestwithai.feature.media.dto.CreateMediaFolderRequest;
import vn.hoidanit.springrestwithai.feature.media.dto.MediaFilterRequest;
import vn.hoidanit.springrestwithai.feature.media.dto.MediaFolderResponse;
import vn.hoidanit.springrestwithai.feature.media.dto.MediaResponse;
import vn.hoidanit.springrestwithai.feature.media.dto.MoveMediaRequest;
import vn.hoidanit.springrestwithai.feature.media.dto.UpdateMediaFolderRequest;

@RestController
@RequestMapping("/api/v1/media")
public class MediaController {

    private final MediaService mediaService;

    public MediaController(MediaService mediaService) {
        this.mediaService = mediaService;
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<MediaResponse>> upload(
            @RequestParam MultipartFile file,
            @RequestParam(required = false) String title,
            @RequestParam(required = false) Long folderId,
            @AuthenticationPrincipal Jwt jwt) {
        Long userId = jwt.getClaim("userId");
        MediaResponse response = mediaService.upload(file, title, folderId, userId);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created("Upload media thành công", response));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<ResultPaginationDTO>> findAll(
            MediaFilterRequest filter, Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.success(mediaService.findAll(filter, pageable)));
    }

    @GetMapping("/{id:\\d+}")
    public ResponseEntity<ApiResponse<MediaResponse>> findById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(mediaService.findById(id)));
    }

    @DeleteMapping("/{id:\\d+}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        mediaService.delete(id);
        return ResponseEntity.ok(ApiResponse.success("Xóa media thành công", null));
    }

    @PatchMapping("/move")
    public ResponseEntity<ApiResponse<Void>> moveMedia(
            @Valid @RequestBody MoveMediaRequest request) {
        mediaService.moveMedia(request);
        return ResponseEntity.ok(ApiResponse.success("Đã di chuyển media thành công", null));
    }

    // ─── Media Folders ────────────────────────────────────────────────────────

    @GetMapping("/folders")
    public ResponseEntity<ApiResponse<List<MediaFolderResponse>>> getFolders(
            @RequestParam(required = false) Long parentId,
            @RequestParam(required = false, defaultValue = "false") boolean all) {
        return ResponseEntity.ok(ApiResponse.success(mediaService.getFolders(parentId, all)));
    }

    @PostMapping("/folders")
    public ResponseEntity<ApiResponse<MediaFolderResponse>> createFolder(
            @Valid @RequestBody CreateMediaFolderRequest request,
            @AuthenticationPrincipal Jwt jwt) {
        Long userId = jwt.getClaim("userId");
        MediaFolderResponse response = mediaService.createFolder(request, userId);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created("Tạo thư mục thành công", response));
    }

    @PutMapping("/folders/{id}")
    public ResponseEntity<ApiResponse<MediaFolderResponse>> updateFolder(
            @PathVariable Long id,
            @Valid @RequestBody UpdateMediaFolderRequest request) {
        MediaFolderResponse response = mediaService.updateFolder(id, request);
        return ResponseEntity.ok(ApiResponse.success("Cập nhật thư mục thành công", response));
    }

    @DeleteMapping("/folders/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteFolder(@PathVariable Long id) {
        mediaService.deleteFolder(id);
        return ResponseEntity.ok(ApiResponse.success("Xóa thư mục thành công", null));
    }
}
