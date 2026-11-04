package vn.hoidanit.springrestwithai.gv.attachment;

import java.util.List;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import vn.hoidanit.springrestwithai.dto.ApiResponse;
import vn.hoidanit.springrestwithai.feature.file.FileService;
import vn.hoidanit.springrestwithai.feature.file.dto.FileUploadResponse;
import vn.hoidanit.springrestwithai.gv.attachment.dto.CreateGvAttachmentRequest;
import vn.hoidanit.springrestwithai.gv.attachment.dto.GvAttachmentResponse;
import vn.hoidanit.springrestwithai.gv.util.GvSecurityUtil;

@RestController
@RequestMapping("/api/v1/gv/tasks/{taskId}/attachments")
public class GvAttachmentController {

    private final GvAttachmentService attachmentService;
    private final FileService fileService;

    public GvAttachmentController(GvAttachmentService attachmentService, FileService fileService) {
        this.attachmentService = attachmentService;
        this.fileService = fileService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<GvAttachmentResponse>>> getByTaskId(@PathVariable Long taskId) {
        return ResponseEntity.ok(ApiResponse.success(attachmentService.getByTaskId(taskId)));
    }

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<GvAttachmentResponse>> upload(
            @PathVariable Long taskId,
            @RequestParam(value = "commentId", required = false) Long commentId,
            @RequestParam("file") MultipartFile file) {
        Long userId = GvSecurityUtil.getRequiredCurrentUserId();
        FileUploadResponse uploaded = fileService.upload(file, "materials");
        GvAttachmentResponse response = attachmentService.create(
                taskId,
                commentId,
                userId,
                file.getOriginalFilename(),
                uploaded.fileUrl(),
                uploaded.size(),
                file.getContentType()
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.created("Tải lên file đính kèm thành công", response));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<GvAttachmentResponse>> create(
            @PathVariable Long taskId,
            @Valid @RequestBody CreateGvAttachmentRequest request) {
        Long userId = GvSecurityUtil.getRequiredCurrentUserId();
        GvAttachmentResponse response = attachmentService.create(
                taskId,
                request.commentId(),
                userId,
                request.fileName(),
                request.fileUrl(),
                request.fileSize(),
                request.fileType()
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(response));
    }

    @DeleteMapping("/{attachmentId}")
    public ResponseEntity<ApiResponse<Void>> delete(
            @PathVariable Long taskId,
            @PathVariable Long attachmentId) {
        Long userId = GvSecurityUtil.getRequiredCurrentUserId();
        attachmentService.delete(attachmentId, userId);
        return ResponseEntity.ok(ApiResponse.success("Xóa file đính kèm thành công", null));
    }
}
