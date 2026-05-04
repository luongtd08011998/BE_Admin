package vn.hoidanit.springrestwithai.feature.article.dto;

/**
 * Query GET /api/v1/articles — type: 0=bài viết thường, 1=nổi bật, 2=tin tức (bỏ qua để lấy hết).
 */
public record ArticleFilterRequest(String keyword, Byte type, Byte active) {
}
