package vn.hoidanit.springrestwithai.feature.category;

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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import vn.hoidanit.springrestwithai.dto.ApiResponse;
import vn.hoidanit.springrestwithai.dto.ResultPaginationDTO;
import vn.hoidanit.springrestwithai.feature.article.ArticleService;
import vn.hoidanit.springrestwithai.feature.category.dto.CategoryFilterRequest;
import vn.hoidanit.springrestwithai.feature.category.dto.CategoryResponse;
import vn.hoidanit.springrestwithai.feature.category.dto.CategoryTreeResponseDTO;
import vn.hoidanit.springrestwithai.feature.category.dto.CreateCategoryRequest;
import vn.hoidanit.springrestwithai.feature.category.dto.UpdateCategoryRequest;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/v1/categories")
public class CategoryController {

    private final CategoryService categoryService;
    private final ArticleService articleService;

    public CategoryController(CategoryService categoryService, ArticleService articleService) {
        this.categoryService = categoryService;
        this.articleService = articleService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<ResultPaginationDTO>> filter(
            @ParameterObject CategoryFilterRequest filter,
            @ParameterObject Pageable pageable) {
        ResultPaginationDTO result = categoryService.filter(filter, pageable);
        return ResponseEntity.ok(ApiResponse.success("Lấy danh sách danh mục thành công", result));
    }

    @GetMapping("/roots")
    public ResponseEntity<ApiResponse<List<CategoryResponse>>> getRootCategories() {
        List<CategoryResponse> result = categoryService.getRootCategories();
        return ResponseEntity.ok(ApiResponse.success("Lấy danh mục gốc thành công", result));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<CategoryResponse>> getById(@PathVariable Long id) {
        CategoryResponse response = categoryService.getById(id);
        return ResponseEntity.ok(ApiResponse.success("Lấy thông tin danh mục thành công", response));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<CategoryResponse>> create(
            @Valid @RequestBody CreateCategoryRequest request) {
        CategoryResponse response = categoryService.create(request);
        URI location = URI.create("/api/v1/categories/" + response.id());
        return ResponseEntity.created(location)
                .body(ApiResponse.created("Tạo danh mục thành công", response));
    }

    @PutMapping
    public ResponseEntity<ApiResponse<CategoryResponse>> update(
            @Valid @RequestBody UpdateCategoryRequest request) {
        CategoryResponse response = categoryService.update(request);
        return ResponseEntity.ok(ApiResponse.success("Cập nhật danh mục thành công", response));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        categoryService.delete(id);
        return ResponseEntity.ok(ApiResponse.success("Xóa danh mục thành công", null));
    }

    @GetMapping("/search")
    public ResponseEntity<ApiResponse<List<CategoryResponse>>> searchByName(
            @RequestParam String keyword) {
        List<CategoryResponse> result = categoryService.searchByName(keyword);
        return ResponseEntity.ok(ApiResponse.success("Tìm kiếm danh mục thành công", result));
    }

    /**
     * Danh mục con <strong>trực tiếp</strong> (một cấp) của cha — không đệ quy xuống cháu.
     */
    @GetMapping("/parent/{parentId}")
    public ResponseEntity<ApiResponse<List<CategoryResponse>>> getByParentId(
            @PathVariable Long parentId) {
        List<CategoryResponse> result = categoryService.getByParentId(parentId);
        return ResponseEntity.ok(ApiResponse.success("Lấy danh mục con trực tiếp thành công", result));
    }

    /**
     * Alias RESTful: cùng dữ liệu với {@code GET /parent/{id}} — chỉ con trực tiếp.
     */
    @GetMapping("/{id}/children")
    public ResponseEntity<ApiResponse<List<CategoryResponse>>> getDirectChildren(@PathVariable Long id) {
        List<CategoryResponse> result = categoryService.getByParentId(id);
        return ResponseEntity.ok(ApiResponse.success("Lấy danh mục con trực tiếp thành công", result));
    }

    @GetMapping("/slug/{slug}")
    public ResponseEntity<ApiResponse<CategoryResponse>> getBySlug(@PathVariable String slug) {
        CategoryResponse response = categoryService.getBySlug(slug);
        return ResponseEntity.ok(ApiResponse.success("Lấy thông tin danh mục theo slug thành công", response));
    }

    @GetMapping("/slug/{slug}/articles")
    public ResponseEntity<ApiResponse<ResultPaginationDTO>> getArticlesByCategorySlug(
            @PathVariable String slug,
            @ParameterObject Pageable pageable) {
        CategoryResponse category = categoryService.getBySlug(slug);
        ResultPaginationDTO result = articleService.getArticlesByCategoryTree(category.id(), pageable);
        return ResponseEntity.ok(ApiResponse.success("Lấy bài viết theo slug danh mục thành công", result));
    }

    @GetMapping("/{id}/tree")
    public ResponseEntity<ApiResponse<CategoryTreeResponseDTO>> getCategoryTree(@PathVariable Long id) {
        CategoryTreeResponseDTO response = categoryService.getCategoryTree(id);
        return ResponseEntity.ok(ApiResponse.success("Lấy cây danh mục thành công", response));
    }

    @GetMapping("/tree")
    public ResponseEntity<ApiResponse<List<CategoryTreeResponseDTO>>> getAllAsTree(
            @RequestParam(required = false) String keyword) {
        List<CategoryTreeResponseDTO> result = categoryService.getAllAsTree(keyword);
        return ResponseEntity.ok(ApiResponse.success("Lấy toàn bộ cây danh mục thành công", result));
    }

    @GetMapping("/{id}/articles")
    public ResponseEntity<ApiResponse<ResultPaginationDTO>> getArticlesByCategoryTree(
            @PathVariable Long id,
            @ParameterObject Pageable pageable) {
        ResultPaginationDTO result = articleService.getArticlesByCategoryTree(id, pageable);
        return ResponseEntity.ok(ApiResponse.success("Lấy bài viết theo cây danh mục thành công", result));
    }
}
