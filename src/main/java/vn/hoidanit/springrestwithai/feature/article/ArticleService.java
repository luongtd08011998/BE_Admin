package vn.hoidanit.springrestwithai.feature.article;

import org.springframework.data.domain.Pageable;

import vn.hoidanit.springrestwithai.dto.ResultPaginationDTO;
import vn.hoidanit.springrestwithai.feature.article.dto.ArticleFilterRequest;
import vn.hoidanit.springrestwithai.feature.article.dto.ArticleResponse;
import vn.hoidanit.springrestwithai.feature.article.dto.CreateArticleRequest;
import vn.hoidanit.springrestwithai.feature.article.dto.UpdateArticleRequest;

public interface ArticleService {

    ArticleResponse create(CreateArticleRequest request);

    ArticleResponse update(UpdateArticleRequest request);

    ArticleResponse getById(Long id);

    ArticleResponse getBySlug(String slug);

    void incrementViewsBySlug(String slug);

    ResultPaginationDTO search(String keyword, Pageable pageable);

    ResultPaginationDTO filter(ArticleFilterRequest filter, Pageable pageable);

    void delete(Long id);

    ResultPaginationDTO getArticlesByCategoryTree(Long categoryId, Pageable pageable);

    ResultPaginationDTO getArticlesByTag(Long tagId, Pageable pageable);

    ResultPaginationDTO getArticlesByTagName(String tagName, Pageable pageable);

    ResultPaginationDTO getRelatedArticles(Long articleId, Pageable pageable);

    ResultPaginationDTO getFeaturedArticles(Pageable pageable);
}
