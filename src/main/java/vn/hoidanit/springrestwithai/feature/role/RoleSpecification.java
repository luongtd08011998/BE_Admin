package vn.hoidanit.springrestwithai.feature.role;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.criteria.Predicate;

import org.springframework.data.jpa.domain.Specification;

import vn.hoidanit.springrestwithai.feature.role.dto.RoleFilterRequest;

public class RoleSpecification {

    public static Specification<Role> build(RoleFilterRequest filter) {
        return (from, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (filter.name() != null && !filter.name().isBlank()) {
                predicates.add(
                        cb.like(cb.lower(from.get("name")),
                                "%" + filter.name().toLowerCase() + "%"));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
