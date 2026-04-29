package vn.hoidanit.springrestwithai.qlkh;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import vn.hoidanit.springrestwithai.feature.feedback.FeedbackRepository;
import vn.hoidanit.springrestwithai.feature.feedback.entity.Feedback;
import vn.hoidanit.springrestwithai.feature.feedback.entity.IssueType;
import vn.hoidanit.springrestwithai.feature.file.FileService;
import vn.hoidanit.springrestwithai.feature.file.dto.FileUploadResponse;

import java.util.ArrayList;
import java.util.List;
import vn.hoidanit.springrestwithai.feature.email.EmailService;

@Service
public class FeedbackService {

    private final FeedbackRepository feedbackRepository;
    private final FileService fileService;
    private final EmailService emailService;

    public FeedbackService(FeedbackRepository feedbackRepository, FileService fileService, EmailService emailService) {
        this.feedbackRepository = feedbackRepository;
        this.fileService = fileService;
        this.emailService = emailService;
    }

    @Transactional("primaryTransactionManager")
    public String createFeedback(Integer customerId, String digiCode, String issueTypeStr, String location, String description, List<MultipartFile> images) {
        
        // 1. Validate IssueType
        IssueType issueType = IssueType.fromString(issueTypeStr);
        if (issueType == null) {
            throw new IllegalArgumentException("Loại vấn đề không hợp lệ. Chỉ chấp nhận: leak, quality, pressure, outage, billing, meter, other");
        }

        // 2. Validate Images count
        if (images != null && images.size() > 5) {
            throw new IllegalArgumentException("Chỉ được phép đính kèm tối đa 5 hình ảnh.");
        }

        // 3. Upload images
        List<String> imageUrls = new ArrayList<>();
        if (images != null && !images.isEmpty()) {
            for (MultipartFile file : images) {
                if (file != null && !file.isEmpty()) {
                    FileUploadResponse uploadRes = fileService.upload(file, "feedbacks");
                    imageUrls.add(uploadRes.fileUrl());
                }
            }
        }

        // 4. Save Feedback to DB to get ID
        Feedback feedback = new Feedback();
        feedback.setCustomerId(customerId);
        feedback.setDigiCode(digiCode);
        feedback.setIssueType(issueType);
        feedback.setLocation(location);
        feedback.setDescription(description);
        feedback.setImages(imageUrls);
        
        // Lưu tạm để lấy ID
        feedback = feedbackRepository.save(feedback);

        // 5. Generate tracking code: PH + digiCode + padded ID (3 digits)
        String trackingCode = String.format("PH%s-%03d", digiCode, feedback.getId());
        feedback.setTrackingCode(trackingCode);
        
        // Cập nhật lại tracking code
        feedbackRepository.save(feedback);

        // 6. Gửi email thông báo bất đồng bộ
        emailService.sendFeedbackEmail(feedback);

        return trackingCode;
    }

    /**
     * Lấy danh sách phản ánh của khách hàng (mới nhất lên đầu).
     */
    @Transactional(value = "primaryTransactionManager", readOnly = true)
    public List<Feedback> getMyFeedbacks(Integer customerId) {
        return feedbackRepository.findByCustomerIdOrderByCreatedAtDesc(customerId);
    }
}
