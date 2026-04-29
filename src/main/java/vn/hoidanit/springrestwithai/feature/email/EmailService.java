package vn.hoidanit.springrestwithai.feature.email;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import vn.hoidanit.springrestwithai.config.FileUploadProperties;
import vn.hoidanit.springrestwithai.feature.feedback.entity.Feedback;

import java.io.File;
import java.nio.file.Path;
import java.util.List;

@Service
public class EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailService.class);

    private final JavaMailSender mailSender;
    private final FileUploadProperties uploadProperties;

    @Value("${spring.mail.username}")
    private String senderEmail;

    // Email của bộ phận CSKH để nhận thông báo
    // Có thể cấu hình trong application.yml (c.app.cskh-email), tạm thời dùng luôn email người gửi
    @Value("${app.cskh-email:${spring.mail.username}}")
    private String cskhEmail;

    public EmailService(JavaMailSender mailSender, FileUploadProperties uploadProperties) {
        this.mailSender = mailSender;
        this.uploadProperties = uploadProperties;
    }

    /**
     * Hàm gửi email bất đồng bộ (Async) thông báo có phản ánh mới.
     * Quá trình này sẽ diễn ra dưới nền, không làm block thread của REST API.
     */
    @Async
    public void sendFeedbackEmail(Feedback feedback) {
        log.info("[EmailService] Bắt đầu gửi email thông báo phản ánh: {}", feedback.getTrackingCode());
        
        try {
            MimeMessage message = mailSender.createMimeMessage();
            // true = multipart (hỗ trợ đính kèm file và HTML)
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(senderEmail, "Hệ thống Nước sạch - CSKH");
            helper.setTo(cskhEmail);
            helper.setSubject("[PHẢN ÁNH MỚI] Mã: " + feedback.getTrackingCode() + " - " + feedback.getIssueType().name());

            // Build HTML Content
            String htmlContent = buildHtmlContent(feedback);
            helper.setText(htmlContent, true); // true = isHtml

            // Đính kèm hình ảnh
            attachImages(helper, feedback.getImages());

            mailSender.send(message);
            log.info("[EmailService] Gửi email phản ánh {} thành công tới {}", feedback.getTrackingCode(), cskhEmail);

        } catch (Exception e) {
            log.error("[EmailService] Lỗi khi gửi email thông báo phản ánh {}: {}", feedback.getTrackingCode(), e.getMessage(), e);
        }
    }

    private String buildHtmlContent(Feedback feedback) {
        return "<h2>Thông báo: Có phản ánh mới từ Khách hàng</h2>" +
                "<table border='1' cellpadding='10' cellspacing='0' style='border-collapse: collapse;'>" +
                "<tr><td style='background-color:#f2f2f2;'><b>Mã Tracking</b></td><td>" + feedback.getTrackingCode() + "</td></tr>" +
                "<tr><td style='background-color:#f2f2f2;'><b>Mã KH (DigiCode)</b></td><td>" + feedback.getDigiCode() + "</td></tr>" +
                "<tr><td style='background-color:#f2f2f2;'><b>Loại sự cố</b></td><td>" + feedback.getIssueType().name() + "</td></tr>" +
                "<tr><td style='background-color:#f2f2f2;'><b>Vị trí / Địa chỉ</b></td><td>" + feedback.getLocation() + "</td></tr>" +
                "<tr><td style='background-color:#f2f2f2;'><b>Mô tả chi tiết</b></td><td>" + feedback.getDescription().replace("\n", "<br>") + "</td></tr>" +
                "<tr><td style='background-color:#f2f2f2;'><b>Số lượng ảnh</b></td><td>" + (feedback.getImages() != null ? feedback.getImages().size() : 0) + " ảnh đính kèm</td></tr>" +
                "<tr><td style='background-color:#f2f2f2;'><b>Thời gian gửi</b></td><td>" + feedback.getCreatedAt() + "</td></tr>" +
                "</table>" +
                "<br/><p>Vui lòng kiểm tra file đính kèm để xem chi tiết hình ảnh (nếu có).</p>";
    }

    private void attachImages(MimeMessageHelper helper, List<String> imageUrls) throws MessagingException {
        if (imageUrls == null || imageUrls.isEmpty()) {
            return;
        }

        // Base URL config từ app
        // File URL có dạng: /uploads/feedbacks/123456789_photo.jpg
        Path uploadRoot = uploadProperties.getUploadRoot(); // Thường là ./uploads

        for (String url : imageUrls) {
            try {
                // Tách lấy tên file từ URL: /uploads/feedbacks/abc.jpg -> feedbacks/abc.jpg
                String relativePath = url.startsWith("/uploads/") ? url.substring(9) : url;
                
                Path filePath = uploadRoot.resolve(relativePath).normalize();
                File file = filePath.toFile();

                if (file.exists() && file.isFile()) {
                    FileSystemResource resource = new FileSystemResource(file);
                    // Đính kèm file
                    helper.addAttachment(file.getName(), resource);
                    log.debug("Đã đính kèm file: {}", file.getName());
                } else {
                    log.warn("Không tìm thấy file hình ảnh để đính kèm: {}", filePath.toAbsolutePath());
                }
            } catch (Exception e) {
                log.error("Lỗi đính kèm file hình ảnh URL {}: {}", url, e.getMessage());
            }
        }
    }
}
