package vn.hoidanit.springrestwithai.gv.label.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateGvLabelRequest(
    @NotBlank(message = "Tên nhãn không được để trống")
    @Size(max = 100, message = "Tên nhãn tối đa 100 ký tự")
    String name,

    @Size(max = 20, message = "Mã màu tối đa 20 ký tự")
    String color,

    String description
) {}
