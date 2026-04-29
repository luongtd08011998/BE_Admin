package vn.hoidanit.springrestwithai.feature.feedback;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import vn.hoidanit.springrestwithai.feature.feedback.entity.FeedbackReply;

import java.util.List;

@Repository
public interface FeedbackReplyRepository extends JpaRepository<FeedbackReply, Long> {

    List<FeedbackReply> findByFeedbackIdOrderByCreatedAtAsc(Long feedbackId);
}
