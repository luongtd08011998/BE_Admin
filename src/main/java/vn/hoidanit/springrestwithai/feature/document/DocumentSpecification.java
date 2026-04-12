package vn.hoidanit.springrestwithai.feature.document;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.criteria.Predicate;

import org.springframework.data.jpa.domain.Specification;

import vn.hoidanit.springrestwithai.feature.document.dto.DocumentFilterRequest;

public class DocumentSpecification {

    public static Specification<Document> build(DocumentFilterRequest filter) {
        return (from, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (filter.title() != null && !filter.title().isBlank()) {
                predicates.add(
                        cb.like(cb.lower(from.get("title")),
                                "%" + filter.title().toLowerCase() + "%"));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
