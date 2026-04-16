package vn.hoidanit.springrestwithai.feature.article;

import jakarta.validation.Valid;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
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
import vn.hoidanit.springrestwithai.feature.article.dto.ArticleFilterRequest;
import vn.hoidanit.springrestwithai.feature.article.dto.ArticleResponse;
import vn.hoidanit.springrestwithai.feature.article.dto.CreateArticleRequest;
import vn.hoidanit.springrestwithai.feature.article.dto.UpdateArticleRequest;

import java.net.URI;

@RestController
@RequestMapping("/api/v1/articles")
public class ArticleController {

    private final ArticleService articleService;

    public ArticleController(ArticleService articleService) {
        this.articleService = articleService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<ResultPaginationDTO>> filter(
            @ParameterObject ArticleFilterRequest filter,
            @ParameterObject Pageable pageable) {
        ResultPaginationDTO result = articleService.filter(filter, pageable);
        return ResponseEntity.ok(ApiResponse.success("Lấy danh sách bài viết thành công", result));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ArticleResponse>> getById(@PathVariable Long id) {
        ArticleResponse response = articleService.getById(id);
        return ResponseEntity.ok(ApiResponse.success("Lấy thông tin bài viết thành công", response));
    }

    @GetMapping("/slug/{slug}")
    public ResponseEntity<ApiResponse<ArticleResponse>> getBySlug(@PathVariable String slug) {
        ArticleResponse response = articleService.getBySlug(slug);
        return ResponseEntity.ok(ApiResponse.success("Lấy thông tin bài viết thành công", response));
    }

    @PostMapping("/slug/{slug}/view")
    public ResponseEntity<Void> recordView(@PathVariable String slug) {
        articleService.incrementViewsBySlug(slug);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @GetMapping("/search")
    public ResponseEntity<ApiResponse<ResultPaginationDTO>> search(
            @RequestParam(required = false) String keyword,
            @ParameterObject Pageable pageable) {
        ResultPaginationDTO result = articleService.search(keyword, pageable);
        return ResponseEntity.ok(ApiResponse.success("Tìm kiếm bài viết thành công", result));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<ArticleResponse>> create(
            @Valid @RequestBody CreateArticleRequest request) {
        ArticleResponse response = articleService.create(request);
        URI location = URI.create("/api/v1/articles/" + response.id());
        return ResponseEntity.created(location)
                .body(ApiResponse.created("Tạo bài viết thành công", response));
    }

    @PutMapping
    public ResponseEntity<ApiResponse<ArticleResponse>> update(
            @Valid @RequestBody UpdateArticleRequest request) {
        ArticleResponse response = articleService.update(request);
        return ResponseEntity.ok(ApiResponse.success("Cập nhật bài viết thành công", response));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        articleService.delete(id);
        return ResponseEntity.ok(ApiResponse.success("Xóa bài viết thành công", null));
    }

    @GetMapping("/{id}/related")
    public ResponseEntity<ApiResponse<ResultPaginationDTO>> getRelatedArticles(
            @PathVariable Long id,
            @ParameterObject Pageable pageable) {
        ResultPaginationDTO result = articleService.getRelatedArticles(id, pageable);
        return ResponseEntity.ok(ApiResponse.success("Lấy bài viết liên quan thành công", result));
    }
}
