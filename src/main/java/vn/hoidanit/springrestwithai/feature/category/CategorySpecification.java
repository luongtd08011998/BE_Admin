package vn.hoidanit.springrestwithai.feature.category;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.criteria.Predicate;

import org.springframework.data.jpa.domain.Specification;

import vn.hoidanit.springrestwithai.feature.category.dto.CategoryFilterRequest;

public class CategorySpecification {

    public static Specification<Category> build(CategoryFilterRequest filter) {
        return (from, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (filter.keyword() != null && !filter.keyword().isBlank()) {
                String pattern = "%" + filter.keyword().toLowerCase() + "%";
                predicates.add(cb.or(
                        cb.like(cb.lower(from.get("name")), pattern),
                        cb.like(cb.lower(from.get("slug")), pattern)));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
