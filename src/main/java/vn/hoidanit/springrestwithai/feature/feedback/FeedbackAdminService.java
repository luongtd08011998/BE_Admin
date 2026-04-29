package vn.hoidanit.springrestwithai.feature.feedback;

import org.springframework.data.domain.Pageable;
import vn.hoidanit.springrestwithai.dto.ResultPaginationDTO;
import vn.hoidanit.springrestwithai.feature.feedback.dto.*;

import java.util.List;

public interface FeedbackAdminService {

    ResultPaginationDTO getAll(FeedbackFilterRequest filter, Pageable pageable);

    FeedbackDetailResponse getById(Long id);

    FeedbackAdminResponse updateStatus(Long id, UpdateFeedbackStatusRequest request);

    FeedbackReplyResponse addReply(Long feedbackId, Long userId, CreateFeedbackReplyRequest request);

    List<FeedbackReplyResponse> getReplies(Long feedbackId);

    FeedbackStatisticsResponse getStatistics();
}
