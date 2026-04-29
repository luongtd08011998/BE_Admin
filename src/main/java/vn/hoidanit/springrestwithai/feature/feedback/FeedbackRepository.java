package vn.hoidanit.springrestwithai.feature.feedback;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import vn.hoidanit.springrestwithai.feature.feedback.entity.Feedback;
import vn.hoidanit.springrestwithai.feature.feedback.entity.FeedbackStatus;
import vn.hoidanit.springrestwithai.feature.feedback.entity.IssueType;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface FeedbackRepository extends JpaRepository<Feedback, Long>, JpaSpecificationExecutor<Feedback> {

    List<Feedback> findByCustomerIdOrderByCreatedAtDesc(Integer customerId);

    Optional<Feedback> findByTrackingCode(String trackingCode);

    long countByStatus(FeedbackStatus status);

    long countByIssueType(IssueType issueType);

    long countByCreatedAtBetween(LocalDateTime start, LocalDateTime end);

    @Query(value = """
            SELECT f.location, COUNT(f.id) as cnt
            FROM feedback f
            WHERE f.location IS NOT NULL AND f.location <> ''
            GROUP BY f.location
            ORDER BY cnt DESC
            LIMIT :limit
            """, nativeQuery = true)
    List<Object[]> findHotspotLocations(@Param("limit") int limit);
}
