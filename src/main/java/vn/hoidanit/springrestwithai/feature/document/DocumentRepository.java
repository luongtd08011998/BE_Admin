package vn.hoidanit.springrestwithai.feature.document;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DocumentRepository extends JpaRepository<Document, Long> {

    List<Document> findAllByArticleId(Long articleId);
}
