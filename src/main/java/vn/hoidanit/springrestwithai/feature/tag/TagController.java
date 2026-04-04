package vn.hoidanit.springrestwithai.feature.tag;

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
import vn.hoidanit.springrestwithai.feature.tag.dto.CreateTagRequest;
import vn.hoidanit.springrestwithai.feature.tag.dto.TagResponse;
import vn.hoidanit.springrestwithai.feature.tag.dto.UpdateTagRequest;

import java.net.URI;

@RestController
@RequestMapping("/api/v1/tags")
public class TagController {

    private final TagService tagService;

    public TagController(TagService tagService) {
        this.tagService = tagService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<ResultPaginationDTO>> getAll(@ParameterObject Pageable pageable) {
        ResultPaginationDTO result = tagService.getAll(pageable);
        return ResponseEntity.ok(ApiResponse.success("Lấy danh sách tag thành công", result));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<TagResponse>> getById(@PathVariable Long id) {
        TagResponse response = tagService.getById(id);
        return ResponseEntity.ok(ApiResponse.success("Lấy thông tin tag thành công", response));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<TagResponse>> create(@Valid @RequestBody CreateTagRequest request) {
        TagResponse response = tagService.create(request);
        URI location = URI.create("/api/v1/tags/" + response.id());
        return ResponseEntity.created(location)
                .body(ApiResponse.created("Tạo tag thành công", response));
    }

    @PutMapping
    public ResponseEntity<ApiResponse<TagResponse>> update(@Valid @RequestBody UpdateTagRequest request) {
        TagResponse response = tagService.update(request);
        return ResponseEntity.ok(ApiResponse.success("Cập nhật tag thành công", response));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        tagService.delete(id);
        return ResponseEntity.ok(ApiResponse.success("Xóa tag thành công", null));
    }
}
