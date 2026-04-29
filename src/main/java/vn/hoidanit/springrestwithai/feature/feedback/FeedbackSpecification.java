package vn.hoidanit.springrestwithai.feature.feedback;

import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import vn.hoidanit.springrestwithai.feature.feedback.dto.FeedbackFilterRequest;
import vn.hoidanit.springrestwithai.feature.feedback.entity.Feedback;

import java.util.ArrayList;
import java.util.List;

public class FeedbackSpecification {

    public static Specification<Feedback> build(FeedbackFilterRequest filter, List<Integer> customerIds) {
        return (from, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (filter.keyword() != null && !filter.keyword().isBlank()) {
                String pattern = "%" + filter.keyword().toLowerCase() + "%";
                predicates.add(cb.or(
                        cb.like(cb.lower(from.get("description")), pattern),
                        cb.like(cb.lower(from.get("location")), pattern),
                        cb.like(cb.lower(from.get("trackingCode")), pattern)));
            }

            if (filter.status() != null) {
                predicates.add(cb.equal(from.get("status"), filter.status()));
            }

            if (filter.issueType() != null) {
                predicates.add(cb.equal(from.get("issueType"), filter.issueType()));
            }

            if (customerIds != null && !customerIds.isEmpty()) {
                predicates.add(from.get("customerId").in(customerIds));
            }

            if (filter.createdFrom() != null) {
                predicates.add(cb.greaterThanOrEqualTo(from.get("createdAt"), filter.createdFrom()));
            }

            if (filter.createdTo() != null) {
                predicates.add(cb.lessThanOrEqualTo(from.get("createdAt"), filter.createdTo()));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
