package vn.hoidanit.springrestwithai.feature.notification;

import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import vn.hoidanit.springrestwithai.feature.notification.entity.DeliveryStatus;
import vn.hoidanit.springrestwithai.feature.notification.entity.Notification;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class NotificationSpecification {

    public static Specification<Notification> withFilters(String type, String deliveryStatus,
                                                          Integer customerId,
                                                          LocalDateTime createdFrom, LocalDateTime createdTo) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (type != null && !type.isBlank()) {
                predicates.add(cb.equal(root.get("type"), type));
            }
            if (deliveryStatus != null && !deliveryStatus.isBlank()) {
                predicates.add(cb.equal(root.get("deliveryStatus"), DeliveryStatus.valueOf(deliveryStatus)));
            }
            if (customerId != null) {
                predicates.add(cb.equal(root.get("customerId"), customerId));
            }
            if (createdFrom != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("createdAt"), createdFrom));
            }
            if (createdTo != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("createdAt"), createdTo));
            }

            query.orderBy(cb.desc(root.get("createdAt")));
            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
