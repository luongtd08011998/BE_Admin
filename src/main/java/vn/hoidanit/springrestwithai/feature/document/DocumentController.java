package vn.hoidanit.springrestwithai.feature.document;

import jakarta.validation.Valid;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import vn.hoidanit.springrestwithai.dto.ApiResponse;
import vn.hoidanit.springrestwithai.dto.ResultPaginationDTO;
import vn.hoidanit.springrestwithai.feature.document.dto.CreateDocumentRequest;
import vn.hoidanit.springrestwithai.feature.document.dto.DocumentResponse;
import vn.hoidanit.springrestwithai.feature.document.dto.UpdateDocumentRequest;

import java.net.URI;

@RestController
@RequestMapping("/api/v1/documents")
public class DocumentController {

    private final DocumentService documentService;

    public DocumentController(DocumentService documentService) {
        this.documentService = documentService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<ResultPaginationDTO>> getAll(@ParameterObject Pageable pageable) {
        ResultPaginationDTO result = documentService.getAll(pageable);
        return ResponseEntity.ok(ApiResponse.success("Lấy danh sách bài viết thành công", result));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<DocumentResponse>> getById(@PathVariable Long id) {
        DocumentResponse response = documentService.getById(id);
        return ResponseEntity.ok(ApiResponse.success("Lấy thông tin bài viết thành công", response));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<DocumentResponse>> create(
            @Valid @RequestBody CreateDocumentRequest request) {
        DocumentResponse response = documentService.create(request);
        URI location = URI.create("/api/v1/documents/" + response.id());
        return ResponseEntity.created(location)
                .body(ApiResponse.created("Tạo bài viết thành công", response));
    }

    @PutMapping
    public ResponseEntity<ApiResponse<DocumentResponse>> update(
            @Valid @RequestBody UpdateDocumentRequest request) {
        DocumentResponse response = documentService.update(request);
        return ResponseEntity.ok(ApiResponse.success("Cập nhật bài viết thành công", response));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        documentService.delete(id);
        return ResponseEntity.ok(ApiResponse.success("Xóa bài viết thành công", null));
    }
}
