package vn.hoidanit.springrestwithai.qlkh.feedback;

import org.springframework.web.multipart.MultipartFile;
import vn.hoidanit.springrestwithai.feature.feedback.entity.Feedback;

import java.util.List;

public interface FeedbackService {
    String createFeedback(Integer customerId, String digiCode, String issueTypeStr, String location, String description, List<MultipartFile> images);
    List<Feedback> getMyFeedbacks(Integer customerId);
}
