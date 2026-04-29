package vn.hoidanit.springrestwithai.feature.feedback;

import jakarta.validation.Valid;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import vn.hoidanit.springrestwithai.dto.ApiResponse;
import vn.hoidanit.springrestwithai.dto.ResultPaginationDTO;
import vn.hoidanit.springrestwithai.feature.feedback.dto.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/feedbacks")
public class FeedbackAdminController {

    private final FeedbackAdminService feedbackAdminService;

    public FeedbackAdminController(FeedbackAdminService feedbackAdminService) {
        this.feedbackAdminService = feedbackAdminService;
    }

    @GetMapping("/statistics")
    public ResponseEntity<ApiResponse<FeedbackStatisticsResponse>> getStatistics() {
        FeedbackStatisticsResponse result = feedbackAdminService.getStatistics();
        return ResponseEntity.ok(ApiResponse.success("Lấy thống kê phản ánh thành công", result));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<ResultPaginationDTO>> getAll(
            @ParameterObject FeedbackFilterRequest filter,
            @ParameterObject Pageable pageable) {
        ResultPaginationDTO result = feedbackAdminService.getAll(filter, pageable);
        return ResponseEntity.ok(ApiResponse.success("Lấy danh sách phản ánh thành công", result));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<FeedbackDetailResponse>> getById(@PathVariable Long id) {
        FeedbackDetailResponse result = feedbackAdminService.getById(id);
        return ResponseEntity.ok(ApiResponse.success("Lấy chi tiết phản ánh thành công", result));
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<ApiResponse<FeedbackAdminResponse>> updateStatus(
            @PathVariable Long id,
            @Valid @RequestBody UpdateFeedbackStatusRequest request) {
        FeedbackAdminResponse result = feedbackAdminService.updateStatus(id, request);
        return ResponseEntity.ok(ApiResponse.success("Cập nhật trạng thái phản ánh thành công", result));
    }

    @PostMapping("/{id}/replies")
    public ResponseEntity<ApiResponse<FeedbackReplyResponse>> addReply(
            @PathVariable Long id,
            @org.springframework.security.core.annotation.AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody CreateFeedbackReplyRequest request) {
        Long userId = jwt.getClaim("userId");
        FeedbackReplyResponse result = feedbackAdminService.addReply(id, userId, request);
        return ResponseEntity.ok(ApiResponse.created("Thêm phản hồi thành công", result));
    }

    @GetMapping("/{id}/replies")
    public ResponseEntity<ApiResponse<List<FeedbackReplyResponse>>> getReplies(@PathVariable Long id) {
        List<FeedbackReplyResponse> result = feedbackAdminService.getReplies(id);
        return ResponseEntity.ok(ApiResponse.success("Lấy danh sách phản hồi thành công", result));
    }
}
