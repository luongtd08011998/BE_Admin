package vn.hoidanit.springrestwithai.gv.comment;

import java.util.List;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import vn.hoidanit.springrestwithai.dto.ApiResponse;
import vn.hoidanit.springrestwithai.gv.comment.dto.CreateGvCommentRequest;
import vn.hoidanit.springrestwithai.gv.comment.dto.GvCommentResponse;
import vn.hoidanit.springrestwithai.gv.util.GvSecurityUtil;

@RestController
@RequestMapping("/api/v1/gv/tasks/{taskId}/comments")
public class GvCommentController {

    private final GvCommentService commentService;

    public GvCommentController(GvCommentService commentService) {
        this.commentService = commentService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<GvCommentResponse>>> getByTaskId(@PathVariable Long taskId) {
        return ResponseEntity.ok(ApiResponse.success(commentService.getByTaskId(taskId)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<GvCommentResponse>> create(
            @PathVariable Long taskId,
            @Valid @RequestBody CreateGvCommentRequest request) {
        Long userId = GvSecurityUtil.getRequiredCurrentUserId();
        GvCommentResponse response = commentService.create(taskId, userId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(response));
    }

    @DeleteMapping("/{commentId}")
    public ResponseEntity<ApiResponse<Void>> delete(
            @PathVariable Long taskId,
            @PathVariable Long commentId) {
        Long userId = GvSecurityUtil.getRequiredCurrentUserId();
        commentService.delete(commentId, userId);
        return ResponseEntity.ok(ApiResponse.success(null));
    }
}
