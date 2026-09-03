package vn.hoidanit.springrestwithai.feature.appconfig;

import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import vn.hoidanit.springrestwithai.feature.appconfig.dto.AppConfigFilterRequest;

import java.util.ArrayList;
import java.util.List;

public class AppConfigSpecification {

    public static Specification<AppConfig> build(AppConfigFilterRequest filter) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (filter.platform() != null && !filter.platform().isBlank()) {
                predicates.add(cb.equal(cb.upper(root.get("platform")), filter.platform().toUpperCase()));
            }

            if (filter.active() != null) {
                predicates.add(cb.equal(root.get("active"), filter.active()));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
