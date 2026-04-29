package vn.hoidanit.springrestwithai.feature.article.dto;

import vn.hoidanit.springrestwithai.feature.article.Article;

import java.time.Instant;
import java.util.List;

public record ArticleResponse(
        Long id,
        String title,
        String slug,
        String content,
        String thumbnail,
        byte type,
        int views,
        byte active,
        AuthorInfo author,
        CategoryInfo category,
        List<TagInfo> tags,
        Instant createdAt,
        Instant updatedAt,
        String url
) {
    public record AuthorInfo(Long id, String name) {
    }

    public record CategoryInfo(Long id, String name) {
    }

    public record TagInfo(Long id, String name) {
    }

    public static ArticleResponse fromEntity(Article article) {
        AuthorInfo authorInfo = new AuthorInfo(
                article.getAuthor().getId(),
                article.getAuthor().getName());

        CategoryInfo categoryInfo = article.getCategory() != null
                ? new CategoryInfo(article.getCategory().getId(), article.getCategory().getName())
                : null;

        List<TagInfo> tags = article.getTagArticles().stream()
                .map(ta -> new TagInfo(ta.getTag().getId(), ta.getTag().getName()))
                .toList();

        return new ArticleResponse(
                article.getId(),
                article.getTitle(),
                article.getSlug(),
                article.getContent(),
                article.getThumbnail(),
                article.getType(),
                article.getViews(),
                article.getActive(),
                authorInfo,
                categoryInfo,
                tags,
                article.getCreatedAt(),
                article.getUpdatedAt(),
                null
        );
    }

    public static ArticleResponse fromEntity(Article article, String baseUrl) {
        ArticleResponse base = fromEntity(article);
        if (baseUrl == null || article.getSlug() == null) return base;
        return new ArticleResponse(
                base.id(), base.title(), base.slug(), base.content(), base.thumbnail(),
                base.type(), base.views(), base.active(), base.author(), base.category(),
                base.tags(), base.createdAt(), base.updatedAt(),
                baseUrl + "/" + article.getSlug()
        );
    }
}
