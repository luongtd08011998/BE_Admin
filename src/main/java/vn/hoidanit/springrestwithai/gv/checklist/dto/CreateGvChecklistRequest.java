package vn.hoidanit.springrestwithai.gv.checklist.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateGvChecklistRequest(
    @NotBlank(message = "Nội dung checklist không được để trống")
    @Size(max = 255, message = "Nội dung tối đa 255 ký tự")
    String title,

    Integer sortOrder,

    Boolean isRequired
) {}
