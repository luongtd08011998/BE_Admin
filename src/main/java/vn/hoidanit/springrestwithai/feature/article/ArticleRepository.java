package vn.hoidanit.springrestwithai.feature.article;

import java.util.Optional;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ArticleRepository extends JpaRepository<Article, Long>, JpaSpecificationExecutor<Article> {

        boolean existsBySlug(String slug);

        boolean existsBySlugAndIdNot(String slug, Long id);

        Optional<Article> findBySlug(String slug);

        @Modifying(clearAutomatically = true, flushAutomatically = true)
        @Query("UPDATE Article a SET a.views = a.views + 1 WHERE a.slug = :slug")
        int incrementViewsBySlug(@Param("slug") String slug);

                @Query(value = """
                                                        SELECT DISTINCT a.*
                                                        FROM articles a
                                                        WHERE :keyword IS NULL
                                                               OR TRIM(:keyword) = ''
                                                               OR a.title COLLATE utf8mb4_0900_ai_ci LIKE CONCAT('%', TRIM(:keyword), '%')
                                                               OR a.slug COLLATE utf8mb4_0900_ai_ci LIKE CONCAT('%', TRIM(:keyword), '%')
                                                               OR a.content COLLATE utf8mb4_0900_ai_ci LIKE CONCAT('%', TRIM(:keyword), '%')
                                                        """, countQuery = """
                                                        SELECT COUNT(DISTINCT a.id)
                                                        FROM articles a
                                                        WHERE :keyword IS NULL
                                                               OR TRIM(:keyword) = ''
                                                               OR a.title COLLATE utf8mb4_0900_ai_ci LIKE CONCAT('%', TRIM(:keyword), '%')
                                                               OR a.slug COLLATE utf8mb4_0900_ai_ci LIKE CONCAT('%', TRIM(:keyword), '%')
                                                               OR a.content COLLATE utf8mb4_0900_ai_ci LIKE CONCAT('%', TRIM(:keyword), '%')
                                                        """, nativeQuery = true)
                Page<Article> searchByKeyword(@Param("keyword") String keyword, Pageable pageable);

        Page<Article> findByCategoryIdIn(List<Long> categoryIds, Pageable pageable);

                Page<Article> findDistinctByTagArticlesTagId(Long tagId, Pageable pageable);

        @Query("SELECT DISTINCT a FROM Article a LEFT JOIN a.tagArticles ta " +
               "WHERE a.id != :id AND a.active = 1 " +
               "AND (a.category.id = :categoryId OR ta.tag.id IN :tagIds)")
        List<Article> findRelatedArticles(@Param("id") Long id,
                                          @Param("categoryId") Long categoryId,
                                          @Param("tagIds") List<Long> tagIds);

        @Query("SELECT DISTINCT a FROM Article a " +
               "WHERE a.id != :id AND a.active = 1 AND a.category.id = :categoryId")
        List<Article> findRelatedArticlesByCategoryOnly(@Param("id") Long id,
                                                        @Param("categoryId") Long categoryId);

        @Query("SELECT a FROM Article a WHERE a.type = 1 AND a.active = 1 ORDER BY a.createdAt DESC")
        Page<Article> findFeaturedArticles(Pageable pageable);
}
