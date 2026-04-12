package vn.hoidanit.springrestwithai.feature.user;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;

import org.springframework.data.jpa.domain.Specification;

import vn.hoidanit.springrestwithai.feature.role.Role;
import vn.hoidanit.springrestwithai.feature.user.dto.UserFilterRequest;

public class UserSpecification {

    public static Specification<User> build(UserFilterRequest filter) {
        return (from, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            // Avoid duplicate rows when joining ManyToMany
            if (query != null) {
                query.distinct(true);
            }

            if (filter.keyword() != null && !filter.keyword().isBlank()) {
                String pattern = "%" + filter.keyword().toLowerCase() + "%";
                predicates.add(cb.or(
                        cb.like(cb.lower(from.get("name")), pattern),
                        cb.like(cb.lower(from.get("email")), pattern)));
            }

            if (filter.name() != null && !filter.name().isBlank()) {
                predicates.add(
                        cb.like(cb.lower(from.get("name")),
                                "%" + filter.name().toLowerCase() + "%"));
            }

            if (filter.email() != null && !filter.email().isBlank()) {
                predicates.add(
                        cb.like(cb.lower(from.get("email")),
                                "%" + filter.email().toLowerCase() + "%"));
            }

            if (filter.address() != null && !filter.address().isBlank()) {
                predicates.add(
                        cb.like(cb.lower(from.get("address")),
                                "%" + filter.address().toLowerCase() + "%"));
            }

            if (filter.ageFrom() != null) {
                predicates.add(
                        cb.greaterThanOrEqualTo(from.get("age"), filter.ageFrom()));
            }

            if (filter.ageTo() != null) {
                predicates.add(
                        cb.lessThanOrEqualTo(from.get("age"), filter.ageTo()));
            }

            if (filter.gender() != null) {
                predicates.add(
                        cb.equal(from.get("gender"), filter.gender()));
            }

            if (filter.roleName() != null && !filter.roleName().isBlank()) {
                Join<User, Role> rolesJoin = from.join("roles", JoinType.INNER);
                predicates.add(
                        cb.like(cb.lower(rolesJoin.get("name")),
                                "%" + filter.roleName().toLowerCase() + "%"));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
