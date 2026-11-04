package vn.hoidanit.springrestwithai.gv.department.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateGvDepartmentRequest(
    @NotBlank(message = "Mã phòng ban không được để trống")
    @Size(max = 50, message = "Mã phòng ban tối đa 50 ký tự")
    String code,

    @NotBlank(message = "Tên phòng ban không được để trống")
    @Size(max = 150, message = "Tên phòng ban tối đa 150 ký tự")
    String name,

    String description,
    Long managerId,
    Long parentId
) {}
