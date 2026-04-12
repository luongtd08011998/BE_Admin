package vn.hoidanit.springrestwithai.feature.article;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;

import org.springframework.data.jpa.domain.Specification;

import vn.hoidanit.springrestwithai.feature.article.dto.ArticleFilterRequest;
import vn.hoidanit.springrestwithai.feature.category.Category;

public class ArticleSpecification {

    public static Specification<Article> build(ArticleFilterRequest filter) {
        return (from, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (query != null) {
                query.distinct(true);
            }

            if (filter.keyword() != null && !filter.keyword().isBlank()) {
                String pattern = "%" + filter.keyword().toLowerCase() + "%";
                Join<Article, Category> categoryJoin = from.join("category", JoinType.LEFT);
                predicates.add(cb.or(
                        cb.like(cb.lower(from.get("title")), pattern),
                        cb.like(cb.lower(from.get("slug")), pattern),
                        cb.like(cb.lower(categoryJoin.get("name")), pattern)));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
