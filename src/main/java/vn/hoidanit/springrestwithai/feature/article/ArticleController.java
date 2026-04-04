package vn.hoidanit.springrestwithai.feature.article;

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
    public ResponseEntity<ApiResponse<ResultPaginationDTO>> getAll(@ParameterObject Pageable pageable) {
        ResultPaginationDTO result = articleService.getAll(pageable);
        return ResponseEntity.ok(ApiResponse.success("Lấy danh sách bài viết thành công", result));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ArticleResponse>> getById(@PathVariable Long id) {
        ArticleResponse response = articleService.getById(id);
        return ResponseEntity.ok(ApiResponse.success("Lấy thông tin bài viết thành công", response));
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
}
