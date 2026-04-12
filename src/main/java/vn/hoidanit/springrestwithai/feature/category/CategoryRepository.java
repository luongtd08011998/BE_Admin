package vn.hoidanit.springrestwithai.feature.category;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.Optional;

public interface CategoryRepository extends JpaRepository<Category, Long>, JpaSpecificationExecutor<Category> {

    boolean existsBySlug(String slug);

    boolean existsBySlugAndIdNot(String slug, Long id);

    boolean existsByParentId(Long parentId);

    List<Category> findByNameContainingIgnoreCase(String keyword);

    List<Category> findByParentId(Long parentId);

    List<Category> findByParentIsNull();

    Optional<Category> findBySlugIgnoreCase(String slug);
}
