package vn.hoidanit.springrestwithai.feature.document;

import org.springframework.data.jpa.repository.JpaRepository;
import vn.hoidanit.springrestwithai.util.constant.DocumentStatus;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface DocumentRepository extends JpaRepository<Document, Long> {

    boolean existsBySlug(String slug);

    boolean existsBySlugAndIdNot(String slug, Long id);

    Page<Document> findAllByStatus(DocumentStatus status, Pageable pageable);
}
