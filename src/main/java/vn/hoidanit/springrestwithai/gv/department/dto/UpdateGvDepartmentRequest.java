package vn.hoidanit.springrestwithai.gv.department.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdateGvDepartmentRequest(
    @NotBlank(message = "Tên phòng ban không được để trống")
    @Size(max = 150, message = "Tên phòng ban tối đa 150 ký tự")
    String name,

    String description,
    Long managerId,
    Long parentId,
    Boolean isActive
) {}
