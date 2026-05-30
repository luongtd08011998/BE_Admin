package vn.hoidanit.springrestwithai.qlk.warehouse.dto;

import java.util.List;
import jakarta.validation.constraints.NotNull;

public record AssignWarehouseUsersRequest(
    @NotNull(message = "Danh sách user ID không được để trống")
    List<Long> userIds
) {}
