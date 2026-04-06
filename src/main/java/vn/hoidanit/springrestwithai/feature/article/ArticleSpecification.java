package vn.hoidanit.springrestwithai.feature.article;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;

import org.springframework.data.jpa.domain.PredicateSpecification;

import vn.hoidanit.springrestwithai.feature.article.dto.ArticleFilterRequest;
import vn.hoidanit.springrestwithai.feature.category.Category;

public class ArticleSpecification {

    public static PredicateSpecification<Article> build(ArticleFilterRequest filter) {
        return (from, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (filter.category() != null && !filter.category().isBlank()) {
                Join<Article, Category> categoryJoin = from.join("category", JoinType.INNER);
                predicates.add(cb.equal(categoryJoin.get("slug"), filter.category()));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
