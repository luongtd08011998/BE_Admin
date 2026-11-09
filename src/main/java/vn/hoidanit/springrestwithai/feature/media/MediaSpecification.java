package vn.hoidanit.springrestwithai.feature.media;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.criteria.Predicate;
import vn.hoidanit.springrestwithai.feature.media.dto.MediaFilterRequest;

import org.springframework.data.jpa.domain.Specification;

public class MediaSpecification {

    private MediaSpecification() {}

    public static Specification<Media> build(MediaFilterRequest filter) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (filter != null) {
                if (filter.title() != null && !filter.title().isBlank()) {
                    String pattern = "%" + filter.title().toLowerCase() + "%";
                    predicates.add(cb.like(cb.lower(root.get("title")), pattern));
                }

                if (filter.fileType() != null && !filter.fileType().isBlank()) {
                    predicates.add(cb.equal(cb.lower(root.get("fileType")), filter.fileType().toLowerCase()));
                }

                if (filter.folderId() != null) {
                    predicates.add(cb.equal(root.get("folderId"), filter.folderId()));
                } else if (Boolean.TRUE.equals(filter.rootOnly())) {
                    predicates.add(cb.isNull(root.get("folderId")));
                }
            }

            if (query != null && query.getOrderList().isEmpty()) {
                query.orderBy(cb.desc(root.get("createdAt")));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
