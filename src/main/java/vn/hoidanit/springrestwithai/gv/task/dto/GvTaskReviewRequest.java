package vn.hoidanit.springrestwithai.gv.task.dto;

import jakarta.validation.constraints.NotNull;

public record GvTaskReviewRequest(
    @NotNull(message = "Kết quả duyệt (approved) không được để trống")
    Boolean approved,

    String feedback,

    Integer rating,         // 1 -> 5 sao

    Integer qualityScore,   // 0 -> 100 điểm chất lượng

    Integer timelinessScore // 0 -> 100 điểm tiến độ
) {}
