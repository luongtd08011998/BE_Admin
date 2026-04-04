package vn.hoidanit.springrestwithai.feature.article;

import org.springframework.data.jpa.repository.JpaRepository;

public interface TagArticleRepository extends JpaRepository<TagArticle, Long> {

    void deleteByArticleId(Long articleId);
}
