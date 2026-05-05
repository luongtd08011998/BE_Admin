package vn.hoidanit.springrestwithai.feature.media;

import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import vn.hoidanit.springrestwithai.dto.ApiResponse;
import vn.hoidanit.springrestwithai.dto.ResultPaginationDTO;
import vn.hoidanit.springrestwithai.feature.media.dto.MediaFilterRequest;
import vn.hoidanit.springrestwithai.feature.media.dto.MediaResponse;

import org.springframework.security.oauth2.jwt.Jwt;

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
            @AuthenticationPrincipal Jwt jwt) {
        Long userId = jwt.getClaim("userId");
        MediaResponse response = mediaService.upload(file, title, userId);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created("Upload media thành công", response));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<ResultPaginationDTO>> findAll(
            MediaFilterRequest filter, Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.success(mediaService.findAll(filter, pageable)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<MediaResponse>> findById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(mediaService.findById(id)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        mediaService.delete(id);
        return ResponseEntity.ok(ApiResponse.success("Xóa media thành công", null));
    }
}
